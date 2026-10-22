# NovaHUD (Fabric 1.20.1)

Оригинальный клиентский мод: светящиеся скруглённые панели в стиле «чит-клиентов».

Виджеты: ватермарк (FPS / пинг / время), Target HUD (голова, ник, анимированное HP, дистанция),
список эффектов с таймерами, броня с прочностью, координаты + blocks/s + направление.

Клавиши: **Right Shift** — меню (тумблеры виджетов и тема), **H** — вкл/выкл худ, **J** — следующая тема (Violet, Ocean, Mint, Ember, Rainbow).

## Сборка
Локально: `./gradlew build` (JDK 17), jar в `build/libs/novahud-1.0.0.jar`.
Или залейте папку в GitHub — workflow `.github/workflows/build.yml` соберёт jar во вкладке Actions → Artifacts.

Нужны: Fabric Loader 0.15+, Fabric API 0.92.2+1.20.1.
