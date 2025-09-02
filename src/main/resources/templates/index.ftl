<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>${title}</title>

    <link href="/css/index.css" rel="stylesheet" />
    <link href="/css/chart.css" rel="stylesheet" />
    <link
      rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css"
    />
    <link
      rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.1/font/bootstrap-icons.min.css"
    />
  </head>

  <#include "/components/toolbar.ftl">
  <#include "/components/sidepanel.ftl">
  
  <body>
    <div class="dv-flex-container">
      <#list configs?eval_json as config>
        <div class="dv-chart dv-hidden" data-chart-id="${config.id}">
            <@toolbar id=config.id title=config.title />

          <div class="dv-chart-area">
              <@sidepanel id=config.id title="Controls" />
          </div>

          <div class="dv-tooltip"></div>
        </div>
      </#list>
    </div>

    <script type="module">
      import getter from "/js/utils/getter.js";
      import components from "/js/utils/components.js";

      const configs = JSON.parse("${configs?json_string}");

      document.querySelectorAll("[data-chart-id]").forEach((node) => {
        const id = node.dataset.chartId;
        const config = configs.find((conf) => conf.id === id);

        const ChartClass = getter[config.type];
        const endpoint = window.location.origin + "/api/data?type=any&id=" + id;

        new ChartClass(node, endpoint, config.options).render();
      });
      components.init();
    </script>
  </body>
</html>
