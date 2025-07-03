<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <title>${title}</title>
    <link rel="stylesheet" href="/css/styles.css" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css"
      rel="stylesheet"
    />
  </head>
  <body>
    <div class="container text-center">
      <div class="row">
        <div class="col border border-2 m-3 p-4 rounded">
          <h1>Pie</h1>
          <div id="pie-chart"></div>
        </div>
        <div class="col border border-2 m-3 p-4 rounded">
          <h1>Bar</h1>
          <div id="bar-chart"></div>
        </div>
      </div>
      <div class="row">
        <div class="col border border-2 m-3 p-4 rounded">
          <h1>Line</h1>
          <div id="line-chart"></div>
        </div>
        <div class="col border border-2 m-3 p-4 rounded">
          <h1>Horizontal Bar</h1>
          <div id="horizontal-chart"></div>
        </div>
      </div>
    </div>

    <script type="module">
      import PieChart from "../js/charts/PieChart.js";
      import BarChart from "../js/charts/BarChart.js";
      import LineChart from "../js/charts/LineChart.js";

      const pie = new PieChart("#pie-chart", 150);
      pie.create(${categories});

      const bar = new BarChart("#bar-chart", 500, 300);
      bar.create(${categories}, "#4269d0");

      const line = new LineChart("#line-chart", 500, 300);
      line.create(${coordinates}, "#ff725c");

      const horizontal = new BarChart("#horizontal-chart", 500, 300, true);
      horizontal.create(${categories}, "#69b3a2");
    </script>
  </body>
</html>
