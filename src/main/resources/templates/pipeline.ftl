<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>${pipeline} - Dynamic Visualizations</title>

    <link rel="stylesheet" href="/css/variables.css" />
    <link rel="stylesheet" href="/css/global.css" />
    <link rel="stylesheet" href="/css/pipeline.css" />
    <link rel="stylesheet" href="/css/controls.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.1/font/bootstrap-icons.min.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/gridstack@12.3.3/dist/gridstack.min.css" />
  </head>

  <body>
    <#include "/components/pipeline/sidebar.ftl"> 
    <#include "/components/pipeline/chart/toolbar.ftl"> 
    <#include "/components/pipeline/chart/sidepanel.ftl">

    <div class="dv-layout">
      <@sidebar pipeline=pipeline filters=filters?eval_json />

      <main class="dv-main">
        <div class="dv-tooltip"></div>
        
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
      import components from "/js/utils/modules/components.js";
      import grid from "/js/utils/modules/grid.js";
      import { corpusFilter } from "/js/filter/CorpusFilter.js";

      const configs = JSON.parse("${configs?json_string}");
      grid.init(configs);

      components.initSidepanels();
      components.initDropdowns();
      components.initAccordions();
      corpusFilter.init();
    </script>
  </body>
</html>
