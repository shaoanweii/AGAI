const sharp = require("sharp");

const [input, output] = process.argv.slice(2);

if (!input || !output) {
  throw new Error("Usage: node scripts/key-transparent.cjs <input> <output>");
}

async function main() {
  const { data, info } = await sharp(input)
    .removeAlpha()
    .raw()
    .toBuffer({ resolveWithObject: true });
  const rgba = Buffer.alloc(info.width * info.height * 4);

  for (let source = 0, target = 0; source < data.length; source += 3, target += 4) {
    const red = data[source];
    const green = data[source + 1];
    const blue = data[source + 2];
    const maximum = Math.max(red, green, blue);
    const minimum = Math.min(red, green, blue);
    const chroma = maximum - minimum;
    const luminance = red * 0.2126 + green * 0.7152 + blue * 0.0722;

    let alpha = Math.max((chroma - 4) / 34, (238 - luminance) / 78);
    if (luminance > 233 && chroma < 13) alpha = 0;
    alpha = Math.max(0, Math.min(1, alpha));
    alpha = alpha * alpha * (3 - 2 * alpha);

    rgba[target] = red;
    rgba[target + 1] = green;
    rgba[target + 2] = blue;
    rgba[target + 3] = Math.round(alpha * 255);
  }

  await sharp(rgba, { raw: { width: info.width, height: info.height, channels: 4 } })
    .trim({ background: { r: 0, g: 0, b: 0, alpha: 0 } })
    .extend({ top: 70, bottom: 70, left: 70, right: 70, background: { r: 0, g: 0, b: 0, alpha: 0 } })
    .png({ compressionLevel: 9 })
    .toFile(output);
}

main();
