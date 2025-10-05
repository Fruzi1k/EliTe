# Тестовые Google Maps URLs

Поддерживаемые форматы:

1. **Новый формат (ваш)**: `https://maps.app.goo.gl/K8UP1yhqMYBikKER6` ✅
2. **Классический**: `https://maps.google.com/maps?q=50.0614,19.9366` ✅
3. **Короткий goo.gl**: `https://goo.gl/maps/abc123` ✅
4. **Google.com/maps**: `https://google.com/maps/@50.0614,19.9366,15z` ✅

## Что изменилось:
- Метод `hasValidGoogleMapsUrl()` теперь проверяет `maps.app.goo.gl`
- Метод `onOpenMaps()` стал более толерантным к форматам URL
- Убрана строгая валидация - теперь пытаемся открыть любой непустой URL