<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>${title}</title>

    <link rel="stylesheet" href="/css/variables.css" />
    <link rel="stylesheet" href="/css/global.css" />
    <link rel="stylesheet" href="/css/editor.css" />
    <link rel="stylesheet" href="/css/controls.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.1/font/bootstrap-icons.min.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/gridstack@12.3.3/dist/gridstack.min.css" />
  </head>

  <body>
    <#include "/components/accordion.ftl">

    <div class="dv-layout">
      <aside class="dv-sidebar">
        <div class="dv-sidebar-header">
          <h1 class="dv-bold">Dynamic Visualizations</h1>
        </div>

        <div class="dv-sidebar-body">
          <div class="">Name</div>
          <input type="text" class="w-100" value="main" />

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
          <template id="grid-stack-item-template">
            <div class="grid-stack-item-content">
              <button type="button">Settings</button>
            </div>
          </template>
        </div>
      </div>
    </div>

    <script type="module">
      import components from "/js/utils/modules/components.js";
      import { GridStack } from "https://cdn.jsdelivr.net/npm/gridstack@12.3.3/+esm";

      const container = document.querySelector(".dv-widgets-container")
      const template = document.querySelector("#new-widget-template");
      const widgets = [
        {
          title: "Text",
          icon: "bi bi-fonts"
        },
        {
          title: "Image",
          icon: "bi bi-image"
        },
        {
          title: "Bar Chart",
          icon: "bi bi-bar-chart"
        },
        {
          title: "Pie Chart",
          icon: "bi bi-pie-chart"
        },
        {
          title: "Line Chart",
          icon: "bi bi-graph-up"
        },
        {
          title: "Highlight Text",
          icon: "bi bi-card-text"
        },
        {
          title: "Network 2D",
          icon: "bi bi-diagram-3"
        },
        {
          title: "Map 2D",
          icon: "bi bi-map"
        },
      ];

      widgets.forEach(widget => {
        const node = template.content.cloneNode(true);
        const icon = node.querySelector("i");
        const span = node.querySelector("span");
        icon.className = widget.icon;
        span.textContent = widget.title;
        span.title = widget.title;

        container.appendChild(node);
      })

      
      const grid = GridStack.init({
        minRow: 6,
        float: true,
        acceptWidgets: ".dv-widget-draggable",
      });
      GridStack.setupDragIn(".dv-widget-draggable");

      components.initAccordions();
    </script>
  </body>
</html>
