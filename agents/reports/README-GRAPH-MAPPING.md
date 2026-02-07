# Report JSON Structure - Graph Mapping and Drill-Down Guide

This guide demonstrates how to use the generated JSON reports for creating graphs and enabling drill-down to Jira tickets.

## Overview

The generated JSON reports contain:
1. **Time periods** with metrics and raw data (key, who, when, weight)
2. **Per-period contributor breakdown** for drill-down by person within a time period
3. **Aggregated results** across all periods by contributor
4. **Full dataset** with ticket metadata and direct links to Jira

## JSON Structure

```json
{
  "reportName": "DMC Dev Productivity Report",
  "generatedAt": "2025-02-07T10:31:00Z",
  "startDate": "2025-01-01",
  "endDate": "2025-12-31",

  "timePeriods": [
    {
      "name": "Q3 2025",
      "startDate": "2025-07-01",
      "endDate": "2025-09-30",

      // Overall metrics for this period
      "metrics": {
        "Completed Tickets": {
          "count": 220,
          "totalWeight": 220.0,
          "contributors": ["Uladzimir Klyshevich", "AI Teammate", "Automation for Jira"]
        }
      },

      "score": 220.0,

      // Per-contributor breakdown for THIS PERIOD ONLY
      "contributorBreakdown": {
        "Uladzimir Klyshevich": {
          "metrics": {
            "Completed Tickets": {
              "count": 176,
              "totalWeight": 176.0,
              "contributors": ["Uladzimir Klyshevich"]
            },
            "Moved to In Progress": {
              "count": 3,
              "totalWeight": 3.0
            }
          },
          "score": 0.0
        },
        "AI Teammate": {
          "metrics": {
            "Completed Tickets": {
              "count": 43,
              "totalWeight": 43.0
            }
          }
        },
        "Automation for Jira": {
          "metrics": {
            "Completed Tickets": {
              "count": 1,
              "totalWeight": 1.0
            }
          }
        }
      },

      // Raw data with full ticket metadata
      "dataset": [
        {
          "source": "tracker",
          "metadata": {
            "key": "DMC-123",
            "summary": "Implement login feature",
            "status": "Done",
            "assignee": "Uladzimir Klyshevich",
            "created": "2025-07-02T09:00:00Z",
            "updated": "2025-07-15T14:30:00Z",
            "self": "https://your-jira.atlassian.net/rest/api/2/issue/12345"
          },
          "metrics": {
            "Completed Tickets": {
              "keyTimes": [
                {
                  "when": "2025-07-15T14:30:00Z",
                  "who": "Uladzimir Klyshevich",
                  "weight": 1.0
                }
              ]
            }
          }
        }
      ]
    }
  ],

  // Aggregated across ALL periods
  "aggregated": {
    "byContributor": {
      "Uladzimir Klyshevich": {
        "metrics": {
          "Completed Tickets": {
            "count": 476,
            "totalWeight": 476.0
          },
          "Moved to In Progress": {
            "count": 6,
            "totalWeight": 6.0
          }
        },
        "score": 476.0
      }
    },
    "total": {
      "metrics": {
        "Completed Tickets": {
          "count": 519,
          "totalWeight": 519.0
        }
      },
      "score": 519.0
    }
  }
}
```

## Use Case 1: Create a Bar Chart for Completed Tickets Over Time

**Goal**: Show completed tickets per quarter

### Using jq to extract chart data:

```bash
# Extract period names and completed ticket counts
cat DMC_Dev_Productivity_Report.json | jq -r '
  .timePeriods[] |
  "\(.name),\(.metrics["Completed Tickets"].count)"
'

# Output (CSV for chart):
# Q1 2025,0
# Q2 2025,0
# Q3 2025,220
# Q4 2025,300
```

### JavaScript/TypeScript example:

```typescript
interface ChartData {
  labels: string[];
  datasets: {
    label: string;
    data: number[];
  }[];
}

function generateChartData(reportJson: any): ChartData {
  const labels = reportJson.timePeriods.map(p => p.name);
  const completedTickets = reportJson.timePeriods.map(
    p => p.metrics["Completed Tickets"]?.count || 0
  );

  return {
    labels,
    datasets: [{
      label: "Completed Tickets",
      data: completedTickets
    }]
  };
}

// For Chart.js, D3.js, or any charting library
const chartData = generateChartData(reportJson);
```

## Use Case 2: Drill-Down to Jira Tickets on Click

**Goal**: When user clicks on a bar in the chart, show all tickets for that period with links to Jira

### Extract ticket keys for a specific period:

```bash
# Get all ticket keys for Q3 2025
cat DMC_Dev_Productivity_Report.json | jq -r '
  .timePeriods[] |
  select(.name == "Q3 2025") |
  .dataset[] |
  .metadata.key
' | head -10

# Output:
# DMC-2094
# DMC-2093
# DMC-2092
# ...
```

### JavaScript/TypeScript example with Jira links:

```typescript
interface TicketLink {
  key: string;
  summary: string;
  jiraUrl: string;
  completedAt: string;
  completedBy: string;
}

function getTicketsForPeriod(
  reportJson: any,
  periodName: string,
  jiraBaseUrl: string = "https://your-jira.atlassian.net"
): TicketLink[] {
  const period = reportJson.timePeriods.find(p => p.name === periodName);

  if (!period) return [];

  return period.dataset
    .filter(item => item.metrics["Completed Tickets"]) // Only completed tickets
    .map(item => {
      const keyTime = item.metrics["Completed Tickets"].keyTimes[0];

      return {
        key: item.metadata.key,
        summary: item.metadata.summary,
        jiraUrl: `${jiraBaseUrl}/browse/${item.metadata.key}`,
        completedAt: keyTime.when,
        completedBy: keyTime.who
      };
    });
}

// Usage:
const tickets = getTicketsForPeriod(reportJson, "Q3 2025");

// Display in table or list
tickets.forEach(ticket => {
  console.log(`<a href="${ticket.jiraUrl}">${ticket.key}</a> - ${ticket.summary}`);
  console.log(`  Completed by ${ticket.completedBy} on ${ticket.completedAt}`);
});

// Or open in Jira directly
function openInJira(ticketKey: string) {
  window.open(`https://your-jira.atlassian.net/browse/${ticketKey}`, '_blank');
}
```

## Use Case 3: Per-Contributor Breakdown within a Time Period

**Goal**: "В месяц я вижу конттрибьютеров, а как мне узнать в этот месяц сколько конкретный человек законтребьютал?"

Answer: Use the **contributorBreakdown** field in each time period.

### Extract contributor metrics for a specific period:

```bash
# Get contributor breakdown for Q3 2025
cat DMC_Dev_Productivity_Report.json | jq '
  .timePeriods[] |
  select(.name == "Q3 2025") |
  .contributorBreakdown
'

# Output:
# {
#   "Uladzimir Klyshevich": {
#     "metrics": {
#       "Completed Tickets": { "count": 176, "totalWeight": 176.0 },
#       "Moved to In Progress": { "count": 3, "totalWeight": 3.0 }
#     },
#     "score": 0.0
#   },
#   "AI Teammate": {
#     "metrics": {
#       "Completed Tickets": { "count": 43, "totalWeight": 43.0 }
#     },
#     "score": 0.0
#   },
#   "Automation for Jira": {
#     "metrics": {
#       "Completed Tickets": { "count": 1, "totalWeight": 1.0 }
#     },
#     "score": 0.0
#   }
# }
```

### JavaScript/TypeScript example:

```typescript
interface ContributorStats {
  contributor: string;
  completedTickets: number;
  totalWeight: number;
  otherMetrics: Record<string, {count: number; weight: number}>;
}

function getContributorStatsForPeriod(
  reportJson: any,
  periodName: string
): ContributorStats[] {
  const period = reportJson.timePeriods.find(p => p.name === periodName);

  if (!period || !period.contributorBreakdown) return [];

  return Object.entries(period.contributorBreakdown).map(([name, data]: [string, any]) => {
    const completedTickets = data.metrics["Completed Tickets"];

    // Extract other metrics (excluding Completed Tickets)
    const otherMetrics: Record<string, any> = {};
    Object.entries(data.metrics).forEach(([metricName, metricData]: [string, any]) => {
      if (metricName !== "Completed Tickets") {
        otherMetrics[metricName] = {
          count: metricData.count,
          weight: metricData.totalWeight
        };
      }
    });

    return {
      contributor: name,
      completedTickets: completedTickets?.count || 0,
      totalWeight: completedTickets?.totalWeight || 0,
      otherMetrics
    };
  });
}

// Usage:
const q3Stats = getContributorStatsForPeriod(reportJson, "Q3 2025");

q3Stats.forEach(stat => {
  console.log(`${stat.contributor}: ${stat.completedTickets} tickets (weight: ${stat.totalWeight})`);

  Object.entries(stat.otherMetrics).forEach(([metric, value]) => {
    console.log(`  ${metric}: ${value.count} (weight: ${value.weight})`);
  });
});

// Output:
// Uladzimir Klyshevich: 176 tickets (weight: 176.0)
//   Moved to In Progress: 3 (weight: 3.0)
// AI Teammate: 43 tickets (weight: 43.0)
// Automation for Jira: 1 tickets (weight: 1.0)
```

## Use Case 4: Get Tickets for Specific Contributor in a Period

**Goal**: When clicking on a contributor in the chart, show only their tickets

```bash
# Get all tickets completed by Uladzimir Klyshevich in Q3 2025
cat DMC_Dev_Productivity_Report.json | jq -r '
  .timePeriods[] |
  select(.name == "Q3 2025") |
  .dataset[] |
  select(.metrics["Completed Tickets"].keyTimes[0].who == "Uladzimir Klyshevich") |
  .metadata.key
' | head -10
```

### JavaScript/TypeScript example:

```typescript
function getTicketsForContributor(
  reportJson: any,
  periodName: string,
  contributor: string,
  metricName: string = "Completed Tickets"
): TicketLink[] {
  const period = reportJson.timePeriods.find(p => p.name === periodName);

  if (!period) return [];

  return period.dataset
    .filter(item => {
      const metric = item.metrics[metricName];
      if (!metric) return false;

      return metric.keyTimes.some(kt => kt.who === contributor);
    })
    .map(item => ({
      key: item.metadata.key,
      summary: item.metadata.summary,
      jiraUrl: `https://your-jira.atlassian.net/browse/${item.metadata.key}`,
      completedAt: item.metrics[metricName].keyTimes[0].when,
      completedBy: contributor
    }));
}

// Usage:
const uladzimirsTickets = getTicketsForContributor(
  reportJson,
  "Q3 2025",
  "Uladzimir Klyshevich"
);

console.log(`Uladzimir completed ${uladzimirsTickets.length} tickets in Q3 2025:`);
uladzimirsTickets.forEach(ticket => {
  console.log(`- ${ticket.key}: ${ticket.summary}`);
});
```

## Use Case 5: Stacked Bar Chart by Contributor

**Goal**: Show stacked bar chart with each contributor's contribution per period

```typescript
interface StackedChartData {
  labels: string[];
  datasets: {
    label: string;
    data: number[];
  }[];
}

function generateStackedChartData(reportJson: any): StackedChartData {
  const labels = reportJson.timePeriods.map(p => p.name);

  // Get all unique contributors
  const contributors = new Set<string>();
  reportJson.timePeriods.forEach(period => {
    if (period.contributorBreakdown) {
      Object.keys(period.contributorBreakdown).forEach(c => contributors.add(c));
    }
  });

  // Create dataset for each contributor
  const datasets = Array.from(contributors).map(contributor => ({
    label: contributor,
    data: reportJson.timePeriods.map(period => {
      const breakdown = period.contributorBreakdown?.[contributor];
      return breakdown?.metrics["Completed Tickets"]?.count || 0;
    })
  }));

  return { labels, datasets };
}

// For Chart.js stacked bar chart
const chartData = generateStackedChartData(reportJson);

// Result:
// {
//   labels: ["Q1 2025", "Q2 2025", "Q3 2025", "Q4 2025"],
//   datasets: [
//     {
//       label: "Uladzimir Klyshevich",
//       data: [0, 0, 176, 300]
//     },
//     {
//       label: "AI Teammate",
//       data: [0, 0, 43, 0]
//     },
//     {
//       label: "Automation for Jira",
//       data: [0, 0, 1, 0]
//     }
//   ]
// }
```

## Use Case 6: Get All Raw Data (KeyTimes) for Custom Analysis

**Goal**: Access raw key, who, when, weight data for custom processing

```bash
# Get all KeyTimes with full details for Q3 2025
cat DMC_Dev_Productivity_Report.json | jq '
  .timePeriods[] |
  select(.name == "Q3 2025") |
  .dataset[] |
  {
    key: .metadata.key,
    summary: .metadata.summary,
    keyTimes: .metrics["Completed Tickets"].keyTimes
  }
' | head -20
```

### JavaScript/TypeScript example:

```typescript
interface RawKeyTime {
  ticketKey: string;
  summary: string;
  when: string;
  who: string;
  weight: number;
  metricName: string;
}

function getAllKeyTimesForPeriod(
  reportJson: any,
  periodName: string
): RawKeyTime[] {
  const period = reportJson.timePeriods.find(p => p.name === periodName);

  if (!period) return [];

  const keyTimes: RawKeyTime[] = [];

  period.dataset.forEach(item => {
    Object.entries(item.metrics).forEach(([metricName, metricData]: [string, any]) => {
      metricData.keyTimes.forEach(kt => {
        keyTimes.push({
          ticketKey: item.metadata.key,
          summary: item.metadata.summary,
          when: kt.when,
          who: kt.who,
          weight: kt.weight,
          metricName
        });
      });
    });
  });

  return keyTimes;
}

// Usage: Custom grouping, filtering, aggregation
const allKeyTimes = getAllKeyTimesForPeriod(reportJson, "Q3 2025");

// Group by date
const byDate = allKeyTimes.reduce((acc, kt) => {
  const date = kt.when.split('T')[0];
  if (!acc[date]) acc[date] = [];
  acc[date].push(kt);
  return acc;
}, {});

// Group by contributor
const byContributor = allKeyTimes.reduce((acc, kt) => {
  if (!acc[kt.who]) acc[kt.who] = [];
  acc[kt.who].push(kt);
  return acc;
}, {});
```

## Summary

The JSON structure provides three levels of data access:

1. **Time Period Metrics**: Aggregated metrics for each period
   - Use for: Line/bar charts showing trends over time
   - Path: `.timePeriods[].metrics`

2. **Per-Period Contributor Breakdown**: Contributor metrics within each period
   - Use for: Stacked charts, contributor comparison within a period
   - Path: `.timePeriods[].contributorBreakdown`
   - **NEW**: Answers "how much did X contribute in this period?"

3. **Raw Dataset with KeyTimes**: Full granularity with key, who, when, weight
   - Use for: Drill-down to Jira tickets, custom analysis, time-series
   - Path: `.timePeriods[].dataset[].metrics[].keyTimes`

All data includes ticket metadata for direct linking to Jira via `metadata.key`.
