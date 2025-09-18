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
    <div class="dv-layout">
      <aside class="dv-sidebar">
        <div class="dv-sidebar-header">
          <h1 class="dv-bold">Dynamic Visualizations</h1>
        </div>

        <div class="dv-sidebar-body">
          <div class="dv-label">Name</div>
          <input type="text" class="form-control mb-3" value="main" />

          <button type="button" class="dv-btn-outline w-100 mb-5">
            <i class="bi bi-plus-circle"></i>
            Add generator
          </button>

          <button type="button" class="dv-btn-primary w-100 mb-3">Save</button>
          <button type="button" class="dv-btn-outline w-100 mb-3">
            Cancel
          </button>
        </div>
      </aside>

      <div class="dv-main">
        <div class="grid-stack"></div>
      </div>
    </div>

    <script type="module">
      import { GridStack } from "https://cdn.jsdelivr.net/npm/gridstack@12.3.3/+esm";

      const grid = GridStack.init();
      grid.addWidget({ w: 7, h: 5, content: "BarChart" });
      grid.addWidget({ w: 5, h: 5, content: "HighlightText" });
      grid.addWidget({ w: 5, h: 5, content: "PieChart" });
      grid.addWidget({ w: 7, h: 5, content: "BarChart2" });
    </script>
  </body>
</html>
