# ReportVisualizer - Interactive HTML Reports

## Обзор

**ReportVisualizer** преобразует JSON отчеты в интерактивные HTML страницы с графиками и drill-down функциональностью.

- 📊 **Интерактивные графики** (Bar Chart, Stacked Chart)
- 🔍 **Drill-down по периодам и контрибьюторам**
- 🎯 **Клик на тикет → открыть в Jira**
- 📱 **Responsive дизайн**
- 🚀 **SPA - работает локально без сервера**

## Архитектура

```
ReportVisualizer (Java)
  ↓
  Читает JSON отчет
  ↓
  FreeMarker Template (report_visualizer.ftl)
  ↓
  Генерирует HTML с инжектированным JSON
  ↓
  HTML (Chart.js) - готов для открытия в браузере
```

## Использование

### 1. Через Job Config

Создайте JSON конфиг:

```json
{
  "name": "ReportVisualizer",
  "params": {
    "jsonReportPath": "agents/reports/output/DMC_Dev_Productivity_Report.json",
    "outputHtmlPath": "agents/reports/output/DMC_Dev_Productivity_Report.html"
  }
}
```

Запустите:
```bash
./dmtools.sh run agents/reports/visualize_dev_productivity.json
```

**Параметр `outputHtmlPath` опционален** - если не указан, HTML создастся рядом с JSON (.json → .html).

### 2. Через Java API

```java
ReportVisualizer visualizer = new ReportVisualizer();

// Auto-generate output path
File htmlFile = visualizer.visualize("agents/reports/output/DMC_Monthly_2025.json");

// Or specify output path
File htmlFile = visualizer.visualize(
    "agents/reports/output/DMC_Monthly_2025.json",
    "reports/monthly_report.html"
);

System.out.println("Open: file://" + htmlFile.getAbsolutePath());
```

### 3. Через CLI (Main Method)

```bash
cd dmtools-core
java -cp build/libs/dmtools-v1.7.129-all.jar \
  com.github.istin.dmtools.reporting.ReportVisualizer \
  agents/reports/output/DMC_Dev_Productivity_Report.json
```

## Сгенерированные HTML файлы

Все 4 отчета визуализированы:

| Отчет | JSON | HTML | Размер |
|-------|------|------|--------|
| **Dev Productivity** | DMC_Dev_Productivity_Report.json | DMC_Dev_Productivity_Report.html | 346 KB |
| **Monthly 2025** | DMC_Monthly_2025.json | DMC_Monthly_2025.html | 336 KB |
| **Weekly 2025** | DMC_Weekly_2025.json | DMC_Weekly_2025.html | 339 KB |
| **Bi-Weekly 2025** | DMC_Bi-Weekly_2025.json | DMC_Bi-Weekly_2025.html | 346 KB |

Все файлы в: `agents/reports/output/`

## Открыть в браузере

**macOS:**
```bash
open agents/reports/output/DMC_Dev_Productivity_Report.html
```

**Linux:**
```bash
xdg-open agents/reports/output/DMC_Dev_Productivity_Report.html
```

**Windows:**
```bash
start agents/reports/output/DMC_Dev_Productivity_Report.html
```

**Или просто двойной клик на HTML файл!**

## Возможности визуализации

### 1. Overview Stats

Показывает общую статистику:
- Total Periods (количество периодов)
- Total Contributors (количество контрибьюторов)
- Total Tickets (всего тикетов)
- Total Weight (общий вес/story points)

### 2. Timeline Chart - Bar Graph

**Completed Tickets по периодам**

- Click на столбец → drill-down в этот период
- Показывает количество завершенных тикетов
- Auto-scale для лучшей читабельности

### 3. Stacked Chart - By Contributors

**Stacked Bar Chart с разбивкой по контрибьюторам**

- Каждый контрибьютор - отдельный цвет
- Видно вклад каждого в каждом периоде
- Легенда с именами контрибьюторов

### 4. Drill-Down by Period

**Выбор периода через dropdown:**

1. Выбираете период (Q3 2025, October 2025, Week 42, etc.)
2. Видите:
   - **Contributors in this Period** - карточки с метриками по каждому
   - **Filter by Contributor** - фильтр для просмотра тикетов конкретного человека
   - **Tickets** - список всех тикетов с метаданными

**Click на карточку контрибьютора:**
- Автоматически фильтрует тикеты этого человека

**Click на тикет:**
- Открывает тикет в Jira в новой вкладке
- URL: `https://your-jira.atlassian.net/browse/DMC-123`

### 5. Ticket Metadata

Каждый тикет показывает:
- **Key** (DMC-123)
- **Summary** (описание)
- **Who** (кто выполнил)
- **When** (дата завершения)
- **Weight** (вес)

## FreeMarker Template

Шаблон находится в: `dmtools-core/src/main/resources/ftl/reports/report_visualizer.ftl`

### Переменные в шаблоне:

```freemarker
${reportJson}       - JSON контент отчета (инжектится напрямую)
${jiraBaseUrl}      - Base URL для Jira (из dmtools.env)
${reportFileName}   - Имя JSON файла
```

### Как работает инжект JSON:

```javascript
// В HTML генерируется:
const REPORT_DATA = {"reportName": "...", "timePeriods": [...]};
const JIRA_BASE_URL = 'https://your-jira.atlassian.net';

// JavaScript читает данные напрямую (не через fetch!)
document.addEventListener('DOMContentLoaded', () => {
    initializeReport();
});
```

**Поэтому HTML работает локально без веб-сервера!**

## Технологии

- **Java**: ReportVisualizer.java
- **FreeMarker**: Template engine
- **Chart.js 4.4.0**: Графики (CDN)
- **Vanilla JavaScript**: Логика SPA (без фреймворков)
- **CSS**: Responsive дизайн с градиентами

## Кастомизация

### Изменить цвета контрибьюторов

В `report_visualizer.ftl` найдите:

```javascript
const colors = [
    'rgba(0, 82, 204, 0.8)',     // Blue
    'rgba(255, 86, 48, 0.8)',    // Orange
    'rgba(54, 179, 126, 0.8)',   // Green
    'rgba(255, 171, 0, 0.8)',    // Yellow
    'rgba(101, 84, 192, 0.8)'    // Purple
];
```

### Изменить Jira Base URL

В `dmtools.env`:
```bash
JIRA_BASE_PATH=https://your-company.atlassian.net
```

Или напрямую в коде:
```java
ReportVisualizer visualizer = new ReportVisualizer();
// jiraBaseUrl читается из PropertyReader
```

### Добавить новые графики

В `report_visualizer.ftl` добавьте новый canvas и Chart.js код:

```html
<div class="chart-container">
    <canvas id="myCustomChart"></canvas>
</div>

<script>
function renderMyCustomChart() {
    const ctx = document.getElementById('myCustomChart').getContext('2d');
    new Chart(ctx, {
        type: 'line',
        data: {...},
        options: {...}
    });
}
</script>
```

## Примеры использования

### Batch-генерация всех отчетов

```bash
#!/bin/bash

# Generate all report visualizations
for json_file in agents/reports/output/*.json; do
    ./dmtools.sh run -c "{
        \"name\": \"ReportVisualizer\",
        \"params\": {
            \"jsonReportPath\": \"$json_file\"
        }
    }"
done

echo "✅ All visualizations generated!"
ls -lh agents/reports/output/*.html
```

### CI/CD Integration

```yaml
# .github/workflows/reports.yml
- name: Generate Reports
  run: |
    ./dmtools.sh run agents/reports/dmc_dev_productivity.json
    ./dmtools.sh run agents/reports/visualize_dev_productivity.json

- name: Upload HTML
  uses: actions/upload-artifact@v3
  with:
    name: report-visualizations
    path: agents/reports/output/*.html
```

### Email Reports (HTML Attachment)

```java
File htmlReport = visualizer.visualize("reports/monthly.json");

EmailSender.send()
    .to("team@company.com")
    .subject("Monthly Productivity Report")
    .body("Please find the attached report")
    .attach(htmlReport)
    .send();
```

## Troubleshooting

### Q: HTML файл не открывается

**A:** Проверьте, что JSON файл существует:
```bash
ls -lh agents/reports/output/*.json
```

### Q: Jira ссылки не работают

**A:** Проверьте `JIRA_BASE_PATH` в `dmtools.env`:
```bash
grep JIRA_BASE_PATH dmtools.env
```

### Q: Графики не отображаются

**A:** Проверьте интернет-соединение (Chart.js загружается с CDN).

Или используйте локальную копию Chart.js в template.

### Q: JSON слишком большой (>10MB)

**A:** FreeMarker может медленно обрабатывать большие JSON. Рассмотрите:
- Использование `saveRawMetadata: false` в ReportGenerator
- Фильтрацию данных перед визуализацией
- Разделение на несколько отчетов

## Roadmap

Планируемые улучшения:

- [ ] **Export to PDF** - конвертация HTML → PDF
- [ ] **Custom templates** - выбор разных стилей
- [ ] **Dark mode** - темная тема
- [ ] **Embedded mode** - для iframe в Confluence
- [ ] **Print-friendly** - CSS для печати
- [ ] **Chart export** - скачать график как PNG
- [ ] **Comparison mode** - сравнить два отчета side-by-side
- [ ] **Offline mode** - встроить Chart.js в HTML

## Summary

✅ **ReportVisualizer успешно создан!**

- Java класс: `ReportVisualizer.java`
- Job: `ReportVisualizerJob.java`
- Template: `report_visualizer.ftl`
- 4 HTML визуализации сгенерированы

**Готово к использованию!** 🎉
