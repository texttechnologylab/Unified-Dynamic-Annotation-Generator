<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>${id} - Dynamic Visualizations</title>

    <link rel="stylesheet" href="/css/variables.css" />
    <link rel="stylesheet" href="/css/pages/pipeline.css" />
    <link rel="stylesheet" href="/css/shared/globals.css" />
    <link rel="stylesheet" href="/css/shared/components.css" />
    <link rel="stylesheet" href="/css/shared/controls.css" />
    <link rel="stylesheet" href="/css/shared/chart.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.1/font/bootstrap-icons.min.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/gridstack@12.3.3/dist/gridstack.min.css" />
  </head>

  <body>
    <#include "/pages/pipeline/sidebar.ftl"> 
    <#include "/shared/toolbar.ftl"> 
    <#include "/shared/sidepanel.ftl">

    <div class="dv-layout">
      <@sidebar id=id pipelines=pipelines?eval_json filters=filters?eval_json />

      <main class="dv-main">
        <div class="dv-chart-tooltip"></div>
        
        <div class="grid-stack">
          <#list configs?eval_json as config>
            <div class="grid-stack-item" gs-id="${config.id}">
              <div class="grid-stack-item-content dv-chart" data-dv-chart="${config.id}">
                <@toolbar id=config.id title=config.title />

                <div class="dv-chart-area">
                  <@sidepanel id=config.id title="Controls" />
                </div>
              </div>
            </div>
          </#list>
        </div>
      </main>
    </div>

    <script type="module">
      import grid from "/js/pages/pipeline/grid.js";
      import { corpusFilter } from "/js/pages/pipeline/filter/CorpusFilter.js";
      import sidepanels from "/js/shared/modules/sidepanels.js";
      import accordions from "/js/shared/modules/accordions.js";
      import dropdowns from "/js/shared/modules/dropdowns.js";

      const configs = JSON.parse("${configs?json_string}");
      grid.init(configs);
      corpusFilter.init();

      const dropdown = document.querySelector(".dv-dropdown");
      const trigger = document.querySelector(".dv-pipeline-switcher-trigger");
      trigger.addEventListener("click", () => {
        dropdown.classList.toggle("show");
      });
      document.addEventListener("click", (event) => {
        if (!dropdown.contains(event.target) && !trigger.contains(event.target)) {
          dropdown.classList.remove("show");
        }
      });

      sidepanels.init();
      accordions.init();
      dropdowns.init();
    </script>
  </body>
</html>
