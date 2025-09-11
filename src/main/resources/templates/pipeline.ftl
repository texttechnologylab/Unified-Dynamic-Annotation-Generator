<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>${title}</title>

    <link rel="stylesheet" href="/css/variables.css">
    <link rel="stylesheet" href="/css/global.css">
    <link rel="stylesheet" href="/css/pipeline.css">
    <link rel="stylesheet" href="/css/chart.css">
    <link rel="stylesheet" href="/css/controls.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.1/font/bootstrap-icons.min.css">
  </head>

  <#include "/components/sidebar.ftl">
  <#include "/components/chart/toolbar.ftl">
  <#include "/components/chart/sidepanel.ftl">
  
  <body>
    <div class="dv-layout">
      <@sidebar filters=filters?eval_json />
      
      <main class="dv-main">
        <#list configs?eval_json as config>
          <div class="dv-chart dv-hidden" data-chart-id="${config.id}">
            <@toolbar id=config.id title=config.title />

            <div class="dv-chart-area">
              <@sidepanel id=config.id title="Controls" />
            </div>
      
            <div class="dv-tooltip"></div>
          </div>
        </#list>
      </main>
    </div>

    <script type="module">
      import getter from "/js/utils/modules/getter.js";
      import components from "/js/utils/modules/components.js";
      import { corpusFilter } from "/js/utils/classes/CorpusFilter.js";

      const configs = JSON.parse("${configs?json_string}");

      document.querySelectorAll("[data-chart-id]").forEach((node) => {
        const id = node.dataset.chartId;
        const config = configs.find((conf) => conf.id === id);

        const ChartClass = getter[config.type];
        const endpoint = window.location.origin + "/api/data?id=" + id;

        new ChartClass(node, endpoint, config.options).render();
      });

      components.init();
      corpusFilter.init();
    </script>
  </body>
</html>
