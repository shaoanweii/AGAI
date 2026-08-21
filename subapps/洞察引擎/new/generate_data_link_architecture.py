from PIL import Image, ImageDraw, ImageFont, ImageFilter

OUT = "/Users/Qiu./Desktop/洞察引擎/new/洞察引擎数据链路架构图-高清版.png"
W, H = 3200, 1800

FONT = "/System/Library/Fonts/Hiragino Sans GB.ttc"
TITLE = ImageFont.truetype(FONT, 76)
SUBTITLE = ImageFont.truetype(FONT, 34)
H1 = ImageFont.truetype(FONT, 34)
H2 = ImageFont.truetype(FONT, 28)
BODY = ImageFont.truetype(FONT, 24)
SMALL = ImageFont.truetype(FONT, 21)

BLUE = "#0d6fe8"
BLUE2 = "#075bd3"
NAVY = "#082b6f"
TEXT = "#123b7a"
BORDER = "#a9ccff"
PANEL = "#f6fbff"
CARD = "#ffffff"
PALE = "#eef6ff"
TEAL = "#049e9a"
ORANGE = "#ff9a3d"
LINE = "#93a9bf"


def shadowed_round(draw_img, box, radius=16, fill=CARD, outline=BORDER, width=2, shadow=True):
    x1, y1, x2, y2 = box
    if shadow:
        layer = Image.new("RGBA", (W, H), (0, 0, 0, 0))
        sd = ImageDraw.Draw(layer)
        sd.rounded_rectangle((x1 + 5, y1 + 8, x2 + 5, y2 + 8), radius, fill=(32, 82, 160, 30))
        layer = layer.filter(ImageFilter.GaussianBlur(8))
        draw_img.alpha_composite(layer)
    d = ImageDraw.Draw(draw_img)
    d.rounded_rectangle(box, radius, fill=fill, outline=outline, width=width)
    return d


def text_center(d, box, text, font, fill=TEXT, spacing=4):
    lines = str(text).split("\n")
    widths = [d.textbbox((0, 0), line, font=font)[2] for line in lines]
    heights = [d.textbbox((0, 0), line, font=font)[3] - d.textbbox((0, 0), line, font=font)[1] for line in lines]
    total_h = sum(heights) + spacing * (len(lines) - 1)
    x1, y1, x2, y2 = box
    y = y1 + (y2 - y1 - total_h) / 2
    for i, line in enumerate(lines):
        d.text((x1 + (x2 - x1 - widths[i]) / 2, y), line, font=font, fill=fill)
        y += heights[i] + spacing


def header_card(img, box, title, subtitle=None, header=BLUE, icon=None, body_fill=CARD):
    d = shadowed_round(img, box, 14, body_fill, BORDER, 2)
    x1, y1, x2, y2 = box
    d.rounded_rectangle((x1, y1, x2, y1 + 54), 14, fill=header, outline=header)
    d.rectangle((x1, y1 + 34, x2, y1 + 54), fill=header)
    text_center(d, (x1, y1 + 7, x2, y1 + 52), title, H1, "#ffffff")
    if subtitle:
        text_center(d, (x1 + 20, y1 + 68, x2 - 20, y2 - 16), subtitle, BODY)
    if icon:
        draw_icon(d, icon, x1 + 24, y1 + 13, "#ffffff", 30)
    return d


def simple_card(img, box, title, fill=PALE, outline="#4f83ff", font=BODY, color=TEXT, radius=12, shadow=True):
    d = shadowed_round(img, box, radius, fill, outline, 2, shadow=shadow)
    text_center(d, box, title, font, color)
    return d


def draw_arrow(d, start, end, color=LINE, width=4, head=18):
    x1, y1 = start
    x2, y2 = end
    d.line((x1, y1, x2, y2), fill=color, width=width)
    if abs(x2 - x1) >= abs(y2 - y1):
        if x2 >= x1:
            pts = [(x2, y2), (x2 - head, y2 - head * 0.65), (x2 - head, y2 + head * 0.65)]
        else:
            pts = [(x2, y2), (x2 + head, y2 - head * 0.65), (x2 + head, y2 + head * 0.65)]
    else:
        if y2 >= y1:
            pts = [(x2, y2), (x2 - head * 0.65, y2 - head), (x2 + head * 0.65, y2 - head)]
        else:
            pts = [(x2, y2), (x2 - head * 0.65, y2 + head), (x2 + head * 0.65, y2 + head)]
    d.polygon(pts, fill=color)


def elbow(d, pts, color=LINE, width=4, head=18):
    for a, b in zip(pts, pts[1:-1]):
        d.line((a, b), fill=color, width=width)
    draw_arrow(d, pts[-2], pts[-1], color=color, width=width, head=head)


def branch_up(d, source_box, target_boxes, y_mid, color=LINE):
    sx = (source_box[0] + source_box[2]) / 2
    sy = source_box[1]
    d.line((sx, sy, sx, y_mid), fill=color, width=4)
    xs = [(b[0] + b[2]) / 2 for b in target_boxes]
    d.line((min(xs), y_mid, max(xs), y_mid), fill=color, width=4)
    for b, tx in zip(target_boxes, xs):
        draw_arrow(d, (tx, y_mid), (tx, b[3] + 2), color=color, width=4, head=16)


def branch_down_to(d, source_boxes, target_box, y_mid, color=LINE):
    tx = (target_box[0] + target_box[2]) / 2
    ty = target_box[3]
    xs = [(b[0] + b[2]) / 2 for b in source_boxes]
    d.line((min(xs), y_mid, max(xs), y_mid), fill=color, width=4)
    for b, sx in zip(source_boxes, xs):
        d.line((sx, b[1] - 2, sx, y_mid), fill=color, width=4)
    draw_arrow(d, (tx, y_mid), (tx, ty + 4), color=color, width=4, head=16)


def draw_icon(d, kind, x, y, color=BLUE2, size=34):
    # Lightweight line icons that stay visually consistent.
    s = size
    if kind == "db":
        d.ellipse((x, y, x + s, y + s * 0.32), outline=color, width=3)
        d.rectangle((x, y + s * 0.16, x + s, y + s * 0.74), outline=color, width=3)
        d.arc((x, y + s * 0.58, x + s, y + s * 0.9), 0, 180, fill=color, width=3)
    elif kind == "engine":
        d.rounded_rectangle((x, y, x + s, y + s), 6, outline=color, width=3)
        d.ellipse((x + s * 0.28, y + s * 0.28, x + s * 0.72, y + s * 0.72), outline=color, width=3)
    elif kind == "app":
        d.rectangle((x, y + s * 0.12, x + s, y + s * 0.72), outline=color, width=3)
        d.line((x + s * 0.2, y + s * 0.88, x + s * 0.8, y + s * 0.88), fill=color, width=3)
        d.line((x + s * 0.5, y + s * 0.72, x + s * 0.5, y + s * 0.88), fill=color, width=3)
    elif kind == "route":
        d.line((x, y + s * 0.5, x + s, y + s * 0.5), fill=color, width=3)
        d.ellipse((x, y + s * 0.35, x + s * 0.3, y + s * 0.65), outline=color, width=3)
        d.ellipse((x + s * 0.7, y + s * 0.35, x + s, y + s * 0.65), outline=color, width=3)
    else:
        d.rounded_rectangle((x, y, x + s, y + s), 6, outline=color, width=3)


img = Image.new("RGBA", (W, H), "#f8fbff")
d = ImageDraw.Draw(img)

# Title
d.polygon([(350, 80), (1600, 28), (2850, 80), (2700, 115), (500, 115)], fill="#e7f2ff")
text_center(d, (0, 36, W, 120), "洞察引擎数据链路架构图", TITLE, NAVY)
text_center(d, (0, 125, W, 174), "数据采集 · 引擎处理 · 结果沉淀 · 数据分发 · 业务应用", SUBTITLE, TEXT)

# Layer rail
layers = [
    ("业务应用层", "应用消费", 185, 150),
    ("客户数据层", "客户沉淀", 410, 140),
    ("数据分发层", "平台分发", 650, 210),
    ("引擎处理层", "智能处理", 955, 360),
    ("数据来源层", "数据采集", 1350, 430),
]
for title, sub, y, h in layers:
    simple_card(img, (35, y, 260, y + h), f"{title}\n{sub}", "#ffffff", BORDER, H2)

# Business apps and customer data
app_boxes = []
cust_boxes = []
app_titles = ["chat BI", "BI", "用户运营平台", "线索运营平台"]
cust_titles = ["A客户数据", "B客户数据", "C客户数据", "D客户数据"]
app_xs = [420, 970, 1520, 2070]
for x, title in zip(app_xs, app_titles):
    box = (x, 205, x + 420, 310)
    app_boxes.append(box)
    simple_card(img, box, title, "#ffffff", BLUE, H2)
for x, title in zip(app_xs, cust_titles):
    box = (x, 430, x + 420, 535)
    cust_boxes.append(box)
    simple_card(img, box, title, "#e9f1ff", "#4f83ff", H2)
for cb, ab in zip(cust_boxes, app_boxes):
    draw_arrow(d, ((cb[0] + cb[2]) / 2, cb[1]), ((ab[0] + ab[2]) / 2, ab[3]), color=LINE, width=4)
# Secondary A data to BI relationship
elbow(d, [((cust_boxes[0][0] + cust_boxes[0][2]) / 2, cust_boxes[0][1]), (620, 360), ((app_boxes[1][0] + app_boxes[1][2]) / 2, 360), ((app_boxes[1][0] + app_boxes[1][2]) / 2, app_boxes[1][3])], color="#b9c2cc", width=3, head=14)

# Distribution
dist_box = (300, 650, 3000, 865)
simple_card(img, dist_box, "数据分发工具（客户管理平台）", "#ffffff", ORANGE, H2)
branch_up(d, dist_box, cust_boxes, 585, color=LINE)

# Results and engines
tag_result = (330, 990, 1780, 1100)
lead_result = (1960, 990, 3000, 1100)
simple_card(img, tag_result, "打标结果数据", "#e9f1ff", "#4f83ff", H2)
simple_card(img, lead_result, "线索结果数据", "#e9f1ff", "#4f83ff", H2)
branch_down_to(d, [tag_result, lead_result], dist_box, 930, color=LINE)

insight_engine = (330, 1205, 1780, 1420)
lead_engine = (1960, 1205, 3000, 1420)
header_card(img, insight_engine, "洞察引擎", "标签识别 · 观点抽取 · 情感识别 · 数据后处理", TEAL, "engine")
header_card(img, lead_engine, "线索引擎", "线索识别 · 线索聚合 · 线索评分 · 结果沉淀", BLUE, "engine")
draw_arrow(d, ((insight_engine[0] + insight_engine[2]) / 2, insight_engine[1]), ((tag_result[0] + tag_result[2]) / 2, tag_result[3]), color=LINE)
draw_arrow(d, ((lead_engine[0] + lead_engine[2]) / 2, lead_engine[1]), ((lead_result[0] + lead_result[2]) / 2, lead_result[3]), color=LINE)

# Raw data
public_raw = (300, 1500, 600, 1590)
private_raw = (700, 1500, 3000, 1590)
simple_card(img, public_raw, "原始公域数据", "#e9f1ff", "#4f83ff", H2)
simple_card(img, private_raw, "原始私域数据", "#e9f1ff", "#4f83ff", H2)
draw_arrow(d, ((public_raw[0] + public_raw[2]) / 2, public_raw[1]), ((insight_engine[0] + insight_engine[2]) / 2, insight_engine[3]), color=LINE)
elbow(d, [((private_raw[0] + private_raw[2]) / 2, private_raw[1]), (1600, 1470), ((insight_engine[0] + insight_engine[2]) / 2, 1470), ((insight_engine[0] + insight_engine[2]) / 2, insight_engine[3])], color=LINE)
elbow(d, [((private_raw[0] + private_raw[2]) / 2, private_raw[1]), (1600, 1470), ((lead_engine[0] + lead_engine[2]) / 2, 1470), ((lead_engine[0] + lead_engine[2]) / 2, lead_engine[3])], color=LINE)

# Source bottom row
service_box = (300, 1705, 600, 1785)
collect_box = (300, 1605, 600, 1685)
simple_card(img, service_box, "数据服务商", "#e9f1ff", "#4f83ff", BODY)
simple_card(img, collect_box, "数据采集工具", "#ffffff", ORANGE, BODY)
draw_arrow(d, ((service_box[0] + service_box[2]) / 2, service_box[1]), ((collect_box[0] + collect_box[2]) / 2, collect_box[3]), color=LINE)
draw_arrow(d, ((collect_box[0] + collect_box[2]) / 2, collect_box[1]), ((public_raw[0] + public_raw[2]) / 2, public_raw[3]), color=LINE)

client_titles = ["A客户\n（DaaS）", "B客户\n（SaaS）", "C客户\n（PaaS）", "C客户\n（OaaS）"]
client_xs = [700, 1260, 1820, 2380]
client_boxes = []
for x, title in zip(client_xs, client_titles):
    box = (x, 1705, x + 420, 1785)
    client_boxes.append(box)
    simple_card(img, box, title, "#e9f1ff", "#4f83ff", BODY)
branch_down_to(d, client_boxes, private_raw, 1645, color=LINE)

img = img.convert("RGB")
img.save(OUT, quality=98)
print(OUT)
