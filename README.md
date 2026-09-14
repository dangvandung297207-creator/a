# THE MASTER BLACKSMITH — HỒN CỦA THÉP

**Codename: True Metallurgy** · Minecraft Forge 1.20.1 · Java 17

> *The player doesn't merely craft weapons. The player **FORGES** them.*
> *Every hammer strike matters. Every mistake leaves a mark.*
> *And the best weapon in the world is the one you forged yourself.*

---

## Tóm tắt (Vietnamese)

**The Master Blacksmith** biến việc chế tạo vũ khí/công cụ/giáp kim loại từ *"2 sắt + 1 gậy"*
thành một **nghề rèn thực sự**:

```
Quặng thô → xử lý → nung bloom → nén phôi → chọn khuôn → rèn từng công đoạn
        → tôi (nước/dầu/dung dịch đặc biệt) → mài góc lưỡi → lắp ráp → kiểm định
        → FLAWED / STANDARD / MASTERWORK / LEGENDARY
```

Mỗi món đồ cao cấp là **độc nhất**: lưu tên người rèn, độ tinh khiết, điểm tay nghề,
lịch sử rèn, cách tôi, góc mài — hiển thị trong lore của item.

## Summary (English)

A complete immersive blacksmithing mod: real forge heats with sweet spots, hammer
minigames (in-world + compact UI), 8 metals with full metallurgy profiles, 8 quenching
media, edge-angle grinding, multi-part assembly, 4 quality tiers, legendary blueprints,
a blacksmith journal, NPCs, forge tiers, ruins, automation and full EN + VI localization.

## Tính năng chính / Features

| # | Hệ thống | Tóm tắt |
|---|----------|---------|
| 1 | **Xưởng rèn** | Lò 4 cấp (Primitive→Master), bễ bơm tay + tự động redstone, đe 4 cấp, thùng tôi, đá mài, bàn lắp ráp, giá treo, kệ kim loại — tất cả custom model/texture |
| 2 | **8 kim loại** | Copper, Tin, Bronze, Iron, Steel, Hardened, Damascus, Starfall — mỗi loại có hardness/toughness/sharpness/flexibility/durability/heat-resistance/density + khoảng nhiệt & vùng chuẩn riêng |
| 3 | **Progression** | Raw → Processed → Bloom → Billet → (chọn khuôn) → Blank → Finished |
| 4 | **Nhiệt độ** | Cold/Warm/Low/**Sweet**/High/Overheat/Molten; màu phôi phát sáng theo nhiệt, thanh nhiệt, vùng chuẩn trên UI |
| 5–7 | **Minigame rèn** | Đánh trực tiếp trong world (chuột trái + búa) hoặc thanh timing trong UI; Perfect/Good/Miss/Bad với VFX + âm thanh khác nhau |
| 8 | **Nung lại** | Phôi nguội trên đe → phải nung lại; mỗi lần nung lại trừ điểm (trade-off) |
| 9–10 | **Tôi** | Water / Oil / Salt Water / Herbal / Mineral / Alchemical / Blood / Starfall — mỗi loại đổi toàn bộ profile kim loại + VFX riêng (hơi nước/trắng, khói đen, bọt dầu…) |
| 11 | **Mài** | Góc lưỡi 15°–35° (sắc ↔ bền), đá mòn dần, sửa bằng đá lửa |
| 12–13 | **Lắp ráp** | Kiếm = lưỡi + chắn + cán + chuôi; rìu/cuốc = đầu + cán; giáo = mũi + cán + da; giáp = tấm + da; 8 loại cán ảnh hưởng grip/hồi đòn/khối lượng |
| 14–15 | **Chất lượng** | Điểm 0–100 từ nhiệt + búa + nung lại + tôi + mài + lắp ráp (kỹ năng > may rủi); Flawed/Standard/**Masterwork**/**Legendary** |
| 16–17 | **Bản sắc** | Lore: người rèn, độ tinh khiết, điểm, cách tôi, góc lưỡi, khối lượng, bản vẽ; đồ Legendary đặt tên riêng |
| 18–19 | **Art** | ~190 texture pixel-art 16px + GUI + entity + armor layers, model 3D custom cho mọi block quan trọng |
| 21–23 | **VFX/âm thanh** | Ember/spark/steam/dust particles (giới hạn theo config), 22 sound event + phụ đề |
| 24–25 | **GUI/Journal** | 4 GUI phong cách lò rèn + **Blacksmith's Journal** (tra cứu kim loại, dung dịch tôi, món đang cầm) |
| 26–27 | **Tiến trình** | Lò/nâng cấp mở khóa nhiệt độ & vật liệu mới; bễ tự động bằng redstone, phễu tương thích |
| 28–29 | **Thế giới/NPC** | Tàn tích lò rèn (có rương blueprint), Thợ rèn lữ hành, dân làng Master Smith |
| 30 | **Huyền thoại** | 5 blueprint (King's Edge, Oathkeeper, Stonesplitter, Skypiercer, Aegis) — bắt buộc cho tier Legendary |
| 31 | **Hiệu năng** | Particle caps, tick nhiệt theo nhịp config, sync tiết kiệm |

## Chơi như thế nào (60 giây)

1. Chế **Tongs** (kẹp) + **Hammer** (búa) + **Forge** + **Anvil** + **Barrel**.
2. `Raw ore + Sand` → **Processed** → nung lò thường → **Bloom**.
3. Kẹp bloom vào lò → nung tới **vùng cam (sweet spot)** → gắp ra đe → **đập trái chuột** (hoặc mở UI đe) để nén thành **Billet**.
4. Đặt billet lên đe → mở UI → **chọn khuôn** (S/A/P/R/L) → rèn đủ số nhát mỗi công đoạn.
5. Khi xong + còn nóng → kẹp nhúng **thùng tôi** (nước = cứng/sắc, dầu = dẻo/bền).
6. Đặt lên **đá mài** → chọn góc lưỡi → mài 3 lượt.
7. **Bàn lắp ráp**: ghép lưỡi + chắn + cán + chuôi (+ blueprint cho Legendary).
8. Đọc lore. Ký tên lên huyền thoại của chính bạn.

Chi tiết đầy đủ: [`docs/PROGRESSION.md`](docs/PROGRESSION.md) · [`docs/METALLURGY.md`](docs/METALLURGY.md).

## Build

Yêu cầu: **JDK 17** + mạng (tải Minecraft/Forge lần đầu).

```bash
./gradlew build        # jar tại build/libs/masterblacksmith-1.0.0.jar
./gradlew runClient     # chạy client dev để test
```

Tái sinh asset (models/textures/data đều sinh từ script, đã commit sẵn kết quả):

```bash
python3 tools/gen_models.py tools/gen_item_models.py  # JSON models/blockstates
python3 tools/gen_data.py tools/gen_data2.py          # recipes/loot/tags/worldgen
python3 tools/gen_textures.py                          # PNG (cần Pillow)
```

## Cấu trúc mã nguồn

```
src/main/java/com/masterblacksmith/
  material/   MetalMaterial / HandleMaterial / QuenchLiquid + registries
  forging/    HeatZone, ForgingTemplate, ForgingData (NBT), HammerStrikeHandler,
              QualityCalculator, AssemblyLogic, ItemIdentity, ForgedStats, GrindingProfile
  block/ + blockentity/   lò, bễ, đe, thùng tôi, đá mài, bàn ráp, giá, kệ
  item/       búa, kẹp, phôi, bộ phận, vũ khí/giáp (stats từ NBT), journal, blueprint
  menu/ + client/screen/  4 GUI + journal
  client/particle/ + client/renderer/  ember/spark/steam + BER giá/kệ + NPC renderer
  entity/ worldgen/ event/ network/ util/
src/main/resources/  models, blockstates, lang (en+vi), 190 textures, sounds,
                     88 recipes, loot, tags, worldgen, advancements
tools/  gen_models.py, gen_item_models.py, gen_data.py, gen_data2.py, gen_textures.py
docs/   PROGRESSION.md, METALLURGY.md, AUDIO.md
```

## Trạng thái âm thanh

22 sound event đã định nghĩa (`assets/.../sounds.json` + phụ đề EN/VI). File `.ogg` cuối
cần thu/synthesize riêng — xem [`docs/AUDIO.md`](docs/AUDIO.md). Trong lúc chờ, các hành
động chính vẫn có sẵn âm vanilla lót (đe, lửa, xèo hơi…) nên feedback không bao giờ câm.

## License

MIT — xem [LICENSE](LICENSE).
