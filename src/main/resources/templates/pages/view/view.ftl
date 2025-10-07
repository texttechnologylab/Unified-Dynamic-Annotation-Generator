<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>${id} - Dynamic Visualizations</title>

    <link rel="stylesheet" href="/css/variables.css" />
    <link rel="stylesheet" href="/css/pages/view.css" />
    <link rel="stylesheet" href="/css/shared/globals.css" />
    <link rel="stylesheet" href="/css/shared/components.css" />
    <link rel="stylesheet" href="/css/shared/controls.css" />
    <link rel="stylesheet" href="/css/shared/chart.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.1/font/bootstrap-icons.min.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/gridstack@12.3.3/dist/gridstack.min.css" />
  </head>

  <body>
    <#include "/pages/view/components/sidebar.ftl"> 
    <#include "/pages/view/components/grid.ftl">

    <div class="dv-layout">
      <@sidebar id=id pipelines=pipelines?eval_json />

      <main class="dv-main">
        <div class="dv-chart-tooltip"></div>

        <@grid widgets=widgets?eval_json />
      </main>
    </div>

    <script type="module">
      import View from "/js/pages/view/View.js";

      const widgets = JSON.parse("${widgets?json_string}");
      const view = new View();
      
      view.initGrid(widgets);
      view.initWidgets(widgets);
    </script>
  </body>
</html>
