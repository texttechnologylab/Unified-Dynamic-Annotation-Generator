<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <title>${title}</title>
    <link rel="stylesheet" href="/css/styles.css" />
  </head>
  <body>
    <div class="container">
      <div class="row">
        <div class="col card">
          <h1>Pie</h1>
          <div id="pie-chart"></div>
        </div>
        <div class="col card">
          <h1>Bar</h1>
          <div id="bar-chart"></div>
        </div>
      </div>
      <div class="row">
        <div class="col card">
          <h1>Line</h1>
          <div id="line-chart"></div>
        </div>
        <div class="col card">
          <h1>Horizontal Bar</h1>
          <div id="horizontal-chart"></div>
        </div>
      </div>
      <div class="row">
        <div class="col card">
          <h1>Network 2D</h1>
          <div id="network-2d"></div>
        </div>
      </div>
      <div class="row">
        <div class="col card">
          <h1>Map 2D</h1>
          <div id="map-2d"></div>
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


      import Network2D from "../js/networks/Network2D.js";
      import Map2D from "../js/maps/Map2D.js";

      const network = new Network2D("#network-2d", 800, 600, 10);
      network.create(${network}, "#aaa");

      const map = new Map2D("#map-2d", 800, 600);
      map.create(${features}, "orange");
    </script>
  </body>
</html>
