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
    <#include "/pages/pipeline/grid.ftl">

    <div class="dv-layout">
      <@sidebar id=id pipelines=pipelines?eval_json />

      <main class="dv-main">
        <div class="dv-chart-tooltip"></div>

        <@grid widgets=widgets?eval_json />
      </main>
    </div>

    <script type="module">
      import grid from "/js/pages/pipeline/grid.js";
      import { corpusFilter } from "/js/pages/pipeline/filter/CorpusFilter.js";
      import sidepanels from "/js/shared/modules/sidepanels.js";
      import accordions from "/js/shared/modules/accordions.js";
      import dropdowns from "/js/shared/modules/dropdowns.js";

      const widgets = JSON.parse("${widgets?json_string}");
      grid.init(widgets);
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
