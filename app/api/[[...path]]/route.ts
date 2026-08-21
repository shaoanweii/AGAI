// @ts-expect-error The VOC demo adapter is authored as an ESM module shared with the existing subapp.
import { buildResult, createSeedData } from "@/lib/voc-demo-api.mjs";
// @ts-expect-error The insight demo adapter is shared with the static subapp and authored as ESM.
import { buildInsightResult } from "@/lib/insight-demo-api.mjs";

type RouteContext = {
  params: Promise<{ path?: string[] }>;
};

type JsonRecord = Record<string, unknown>;

let database = createSeedData();

function ok(result: unknown, tid = "agai-voc-demo") {
  return Response.json(
    { success: true, message: "请求成功", code: "200", result, tid },
    { headers: { "Cache-Control": "no-store" } },
  );
}

async function readBody(request: Request): Promise<JsonRecord> {
  if (request.method === "GET" || request.method === "HEAD") return {};
  const contentType = request.headers.get("content-type") || "";
  if (!contentType.includes("application/json")) return {};
  try {
    return (await request.json()) as JsonRecord;
  } catch {
    return {};
  }
}

function isStreamRequest(pathname: string, request: Request) {
  if ((request.headers.get("accept") || "").includes("text/event-stream")) return true;
  if (/\/review\/qa\/ask$/i.test(pathname)) return true;
  return /\/report\/(group-analysis|product-self-analysis|journey-analysis|product-analysis|service-analysis|competitor-compare|vocleadership|keyaccount|hot-event|new-car-launch)\/(?:.*result|.*out|data-source-report)$/i.test(
    pathname,
  );
}

function streamResponse(pathname: string, body: JsonRecord) {
  const comparedNames = [body.firstSelectedName, body.secondSelectedName]
    .map((name) => String(name || "").trim())
    .filter(Boolean);
  const subject = String(body.brandName || body.carSeriesName || "智行汽车集团");
  const chunks =
    pathname.includes("/competitor-compare/") && comparedNames.length === 2
      ? [
          `当前筛选条件下，${comparedNames[0]}与${comparedNames[1]}的客户体验对比已完成。`,
          `${comparedNames[0]}在智能交互和驾乘空间方面提及优势更明显，${comparedNames[1]}在服务响应与交付体验方面表现更均衡。`,
          "建议围绕两者差距最大的系统稳定性、服务闭环和续航解释场景建立专项改善清单。",
        ]
      : [
          `基于当前筛选条件，${subject}客户体验指数保持稳定。`,
          "正向反馈集中在空间、智能交互和服务响应，主要风险来自系统稳定性与问题闭环时效。",
          "建议优先推进高频负面场景专项治理，并将处理结果同步至事件任务和月度报告。",
        ];
  const openAiShape = pathname === "/api/review/qa/ask";
  const payload = chunks
    .map((chunk) =>
      `data: ${
        openAiShape
          ? JSON.stringify({ choices: [{ delta: { content: chunk } }] })
          : chunk
      }\n\n`,
    )
    .join("");
  const end = `data: ${openAiShape ? "[DONE]" : "[END]"}\n\n`;
  return new Response(payload + end, {
    headers: {
      "Content-Type": "text/event-stream; charset=utf-8",
      "Cache-Control": "no-cache",
      Connection: "keep-alive",
    },
  });
}

function downloadResponse() {
  const csv = [
    "编号,主题,状态,负责人,创建时间",
    "VOC-20260807-01,智能座舱稳定性,处理中,产品质量中心,2026-08-07",
    "VOC-20260807-02,售后响应体验,待确认,售后服务中心,2026-08-06",
  ].join("\n");
  return new Response(`\ufeff${csv}`, {
    headers: {
      "Content-Type": "text/csv; charset=utf-8",
      "Content-Disposition": 'attachment; filename="VOCVoice-Demo.csv"',
    },
  });
}

async function handle(request: Request, context: RouteContext) {
  const { path = [] } = await context.params;
  const pathname = `/api/${path.join("/")}`;
  const url = new URL(request.url);
  const body = {
    ...Object.fromEntries(url.searchParams),
    ...(await readBody(request)),
  };

  if (pathname === "/api/local/health") {
    return ok({ status: "ok", product: "VOC智声", dataVersion: database.version });
  }
  if (pathname === "/api/local/runtime") {
    return ok({ mode: "sites-demo", product: "VOC智声", online: true, resetAt: database.resetAt });
  }
  if (pathname === "/api/local/session/enter") {
    return ok({ token: "voc-voice-local-demo-token", user: database.users?.[0] });
  }
  if (pathname === "/api/local/admin/reset" && request.method === "POST") {
    database = createSeedData();
    return ok({ resetAt: database.resetAt });
  }
  const insightResult = buildInsightResult(pathname, request.method, body);
  if (insightResult.handled) {
    return ok(insightResult.result, "agai-insight-demo");
  }
  if (isStreamRequest(pathname, request)) return streamResponse(pathname, body);
  if (/(download|export)/i.test(pathname) && !/(list|find)/i.test(pathname)) {
    return downloadResponse();
  }

  return ok(buildResult(pathname, request.method, body, database));
}

export const GET = handle;
export const POST = handle;
export const PUT = handle;
export const PATCH = handle;
export const DELETE = handle;
export const OPTIONS = () => new Response(null, { status: 204 });
