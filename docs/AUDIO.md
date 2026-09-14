# Soundscape — hướng dẫn hoàn thiện âm thanh

Mod định nghĩa **22 sound event** (`src/main/resources/assets/masterblacksmith/sounds.json`):

- Lò: `forge/crackle`, `forge/roar` · Bễ: `bellows/pump`, `bellows/air`
- Búa: `hammer/iron`, `hammer/steel`, `hammer/master`, `hammer/miss`, `hammer/crack` · `anvil/ring`
- Tôi: `quench/water`, `quench/oil`, `quench/special`
- Mài: `grind/wheel`, `grind/spark`
- Ráp: `assembly/wood`, `assembly/leather`, `assembly/rivet`
- Khác: `journal/page`, `fanfare/masterwork`, `fanfare/legendary`, `tongs/clank`, `tongs/place`

## Thêm file âm thanh

1. Thu/synthesize file `.ogg` (mono 44.1kHz, chuẩn hóa −3dB).
2. Đặt vào `src/main/resources/assets/masterblacksmith/sounds/<path>.ogg`
   khớp với `name` trong `sounds.json` (ví dụ `hammer/steel` → `sounds/hammer/steel.ogg`).
3. Mỗi event phát với **pitch ngẫu nhiên** (`SoundUtil`) nên chỉ cần 1 file/event
   mà không bị lặp nhàm chán; muốn đa dạng hơn thì thêm `sounds: [{...}, {...}]`.

## Trạng thái hiện tại

File `.ogg` chưa kèm trong repo (cần thu âm thật/synthesize có bản quyền sạch).
Để game không bao giờ "câm", code đã **lót âm vanilla** cho các hành động chính:
anvil_use, fire crackle (block tick), fire_extinguish khi tôi, v.v.
Phụ đề (subtitles) đã có đủ EN + VI.
