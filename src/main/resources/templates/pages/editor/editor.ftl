<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Editor - Dynamic Visualizations</title>

    <link rel="stylesheet" href="/css/variables.css" />
    <link rel="stylesheet" href="/css/pages/editor.css" />
    <link rel="stylesheet" href="/css/shared/globals.css" />
    <link rel="stylesheet" href="/css/shared/components.css" />
    <link rel="stylesheet" href="/css/shared/controls.css" />
    <link rel="stylesheet" href="/css/shared/chart.css" />
    <link
      rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css"
    />
    <link
      rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.1/font/bootstrap-icons.min.css"
    />
    <link
      rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/gridstack@12.3.3/dist/gridstack.min.css"
    />
  </head>

  <body>
    <#include "/shared/modal.ftl"> <#include "/pages/editor/sidebar.ftl">

    <div class="dv-layout">
      <@sidebar />

      <div class="dv-main">
        <div class="grid-stack">
          <template id="text-placeholder">
            <div class="grid-stack-item-content">
              The quick brown fox jumps over the lazy dog.
            </div>
          </template>

          <template id="image-placeholder">
            <div class="grid-stack-item-content">
              <img
                src="https://placehold.co/600x400"
                width="100%"
                height="100%"
              />
            </div>
          </template>

          <template id="d3-chart-placeholder">
            <div class="grid-stack-item-content dv-chart">
              <div class="dv-toolbar">
                <button
                  class="dv-btn dv-btn-toolbar"
                  type="button"
                  title="Edit"
                >
                  <i class="bi bi-pencil"></i>
                </button>

                <span class="dv-toolbar-title"></span>

                <button
                  class="dv-btn dv-btn-toolbar"
                  type="button"
                  title="Remove"
                >
                  <i class="bi bi-x-lg"></i>
                </button>
              </div>
              <div class="dv-chart-area"></div>
            </div>
          </template>
        </div>
      </div>

      <@modal />
    </div>

    <script type="module">
      import accordions from "/js/shared/modules/accordions.js";
      import editor from "/js/pages/editor/editor.js";

      const json = JSON.parse("${json?json_string}");
      console.log(json);

      accordions.init();
      editor.init(json);
    </script>
  </body>
</html>
