# True Metallurgy — bảng số liệu

Nhiệt độ tính bằng °C. Sweet spot là khoảng rèn lý tưởng.

## Kim loại

| Metal | Forge window | Sweet spot | Burn | Workability | Base dmg | Base dura |
|---|---|---|---|---|---|---|
| Tin | 300–520 | 350–460 | 700 | 1.40 | 2.0 | 130 |
| Copper | 550–850 | 640–760 | 1050 | 1.25 | 3.0 | 220 |
| Bronze | 650–950 | 740–880 | 1100 | 1.15 | 4.0 | 340 |
| Iron | 750–1050 | 850–1000 | 1300 | 1.00 | 5.0 | 520 |
| Steel | 850–1150 | 950–1100 | 1400 | 0.90 | 6.0 | 920 |
| Hardened Steel | 900–1200 | 1000–1150 | 1450 | 0.80 | 7.0 | 1450 |
| Damascus Steel | 900–1220 | 1000–1175 | 1450 | 0.75 | 7.5 | 1850 |
| Starfall Steel | 1000–1350 | 1100–1300 | 1600 | 0.70 | 9.0 | 2600 |

Mỗi kim loại còn có hardness / toughness / sharpness / flexibility / durability /
heat-resistance / density riêng — xem `MetalMaterials.java` và Journal trong game.

## Dung dịch tôi (multipliers H/T/S/D + tốc độ nguội °C/s)

| Medium | H | T | S | D | Cool/s | Ghi chú |
|---|---|---|---|---|---|---|
| Water | 1.25 | 0.80 | 1.20 | 0.90 | 900 | hơi nước lớn |
| Oil | 1.05 | 1.25 | 1.00 | 1.20 | 350 | khói đen |
| Salt Water | 1.35 | 0.70 | 1.30 | 0.80 | 1100 | hơi nước cực lớn |
| Herbal Oil | 1.10 | 1.30 | 1.05 | 1.35 | 300 | +1 purity |
| Mineral Oil | 1.15 | 1.20 | 1.00 | 1.50 | 280 | khói đen |
| Alchemical Oil | 1.20 | 1.30 | 1.25 | 1.30 | 250 | +2 purity |
| Blood-infused | 1.30 | 1.10 | 1.35 | 1.00 | 500 | khói đen |
| Starfall | 1.40 | 1.40 | 1.40 | 1.40 | 200 | +3 purity |

## Cán (grip / độ bền / khối lượng / hồi đòn)

Oak .60/1.0/.40/+0 · Spruce .55/.9/.35/+.05 · Birch .65/1.0/.38/+.05 ·
Dark Oak .70/1.15/.45/+0 · Bamboo .50/.8/.25/+.15 · Reinforced .75/1.5/.70/−.10 ·
Leather-Wrapped .95/1.2/.50/+.05 · Bone .60/1.3/.55/+0.

## Góc mài

15°: sắc ×1.35 / bền ×0.75 · 25°: ×1.0/×1.0 · 35°: sắc ×0.85 / bền ×1.25
(nội suy tuyến tính ở giữa; góc gợi ý: kiếm 20°, rìu 28°, cuốc 32°, giáo 18°).

## Công thức điểm (0–100)

- **Forging**: 55% điểm búa + 35% điểm nhiệt + (purity−85)×0.5 − 2.5×số lần nung lại.
- **Cuối**: 40% forging + 15% quench + 15% grind + 30% assembly (+4 blueprint),
  chặn bởi trần đe và chặn 89 nếu thiếu blueprint khớp.
- Quá tay (đánh thừa mỗi công đoạn): −0.6×(số nhát thừa) mỗi nhát.
- Tier: Flawed <40 · Standard 40–69 · Masterwork 70–89 · Legendary 90+.
