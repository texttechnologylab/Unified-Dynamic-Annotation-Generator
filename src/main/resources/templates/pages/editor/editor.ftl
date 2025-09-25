<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>${title}</title>

    <link rel="stylesheet" href="/css/variables.css" />
    <link rel="stylesheet" href="/css/pages/editor.css" />
    <link rel="stylesheet" href="/css/shared/global.css" />
    <link rel="stylesheet" href="/css/shared/controls.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.1/font/bootstrap-icons.min.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/gridstack@12.3.3/dist/gridstack.min.css" />
  </head>

  <body>
    <#include "/shared/modal.ftl">
    <#include "/shared/accordion.ftl">

    <div class="dv-layout">
      <aside class="dv-sidebar">
        <div class="dv-sidebar-header">
          <h1 class="dv-bold">Dynamic Visualizations</h1>
        </div>

        <div class="dv-sidebar-body">
          <label>
            Name
            <input type="text" class="w-100" value="main" />
          </label>

          <button type="button" class="dv-btn-outline w-100 my-2">
            <i class="bi bi-plus-circle"></i>
            Add generator
          </button>

          <@accordion icon="bi bi-grid" title="Widgets">
            <p>Add new widgets by dragging them into the grid area.</p>
            <div class="dv-widgets-container">
              <template id="new-widget-template">
                <div class="dv-new-widget">
                  <div class="dv-widget-draggable" title="Drag to add">
                    <i></i>
                  </div>
                  <span class="dv-widget-title"></span>
                </div>
              </template>
            </div>
          </@accordion>
        </div>
      </aside>
      
      <div class="dv-main">
        <div class="grid-stack">
          <template id="text-placeholder">
            <div class="m-2">The quick brown fox jumps over the lazy dog</div>
          </template>

          <template id="image-placeholder">
            <img src="https://placehold.co/600x400" alt="Here goes an image">
          </template>

          <template id="d3-chart-placeholder">
              <div class="dv-toolbar">
                <span class="dv-title dv-bold"></span>
              </div>
              <div class="dv-chart-area">
                <i></i>
              </div>
          </template>
        </div>
      </div>

      <@modal />
    </div>

    <script type="module">
      import accordions from "/js/shared/modules/accordions.js";
      import editor from "/js/pages/editor/editor.js";

      accordions.init();
      editor.init();
    </script>
  </body>
</html>
