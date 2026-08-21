from PIL import Image, ImageDraw, ImageFont

src = "/Users/Qiu./Desktop/洞察引擎/new/洞察引擎产品架构图-支撑箭头位置修正版-3344x1882.png"
out = "/Users/Qiu./Desktop/洞察引擎/new/洞察引擎产品架构图-支撑箭头位置精修版-3344x1882.png"

img = Image.open(src).convert("RGBA")
draw = ImageDraw.Draw(img)

font_path = "/System/Library/Fonts/Hiragino Sans GB.ttc"
font = ImageFont.truetype(font_path, 30)

blue = (20, 103, 221, 255)
light_bg = (248, 251, 255, 255)
soft_bg = (246, 250, 255, 255)

# Clear the old support-arrow area between production and knowledge layers.
# Keep the band in the inter-layer whitespace and away from module card borders.
draw.rectangle((1070, 1088, 2250, 1210), fill=light_bg)

def arrow_line(start, end, width=7, color=blue, head=28):
    draw.line((start, end), fill=color, width=width)
    x1, y1 = start
    x2, y2 = end
    if abs(x2 - x1) < abs(y2 - y1):
        # vertical arrow
        if y2 < y1:
            pts = [(x2, y2), (x2 - head, y2 + head), (x2 + head, y2 + head)]
        else:
            pts = [(x2, y2), (x2 - head, y2 - head), (x2 + head, y2 - head)]
    else:
        # horizontal arrow
        if x2 < x1:
            pts = [(x2, y2), (x2 + head, y2 - head), (x2 + head, y2 + head)]
        else:
            pts = [(x2, y2), (x2 - head, y2 - head), (x2 - head, y2 + head)]
    draw.polygon(pts, fill=color)

def centered_text(text, center, fill=blue):
    bbox = draw.textbbox((0, 0), text, font=font)
    w = bbox[2] - bbox[0]
    h = bbox[3] - bbox[1]
    x = center[0] - w / 2
    y = center[1] - h / 2
    # Small white halo keeps text readable on pale background.
    draw.rounded_rectangle((x - 10, y - 6, x + w + 10, y + h + 8), radius=10, fill=soft_bg)
    draw.text((x, y), text, font=font, fill=fill)

# AI capability support: short vertical arrow starts above Knowledge Center.
draw.ellipse((708, 1200, 732, 1224), fill=blue)
arrow_line((720, 1212), (720, 1126), width=5, head=22)
centered_text("AI能力支撑", (870, 1165))

# Data processing support: one straight vertical arrow above Rules Center.
draw.ellipse((1460, 1200, 1484, 1224), fill=blue)
arrow_line((1472, 1212), (1472, 1126), width=5, head=22)
centered_text("数据处理支撑", (1585, 1165))

img.save(out)
print(out)
