<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <title>${title}</title>
    <link rel="stylesheet" href="/css/styles.css" />
  </head>
  <body>
    <div class="container">
      <div class="card">
        <h1>Bar</h1>
        <div id="bar-chart"></div>
      </div>
      <div class="card">
        <h1>Line</h1>
        <div id="line-chart"></div>
      </div>
      <div class="card">
        <h1>Pie</h1>
        <div id="pie-chart"></div>
      </div>
      <div class="card">
        <h1>Text</h1>
        <div id="highlight-text"></div>
      </div>
      <div class="card">
        <h1>Map</h1>
        <div id="map-2d"></div>
      </div>
      <div class="card">
        <h1>Network</h1>
        <div id="network-2d"></div>
      </div>
    </div>

    <script type="module">
      import BarChart from "../js/charts/BarChart.js";
      import LineChart from "../js/charts/LineChart.js";
      import PieChart from "../js/charts/PieChart.js";
      import TextDocument from "../js/documents/TextDocument.js";
      import Map2D from "../js/maps/Map2D.js";
      import Network2D from "../js/networks/Network2D.js";

      const bar = new BarChart("#bar-chart", "/data/categories.json", 800, 600);
      bar.render();

      const line = new LineChart(
        "#line-chart",
        "/data/coordinates.json",
        800,
        600
      );
      line.render();

      const pie = new PieChart("#pie-chart", "/data/categories.json", 300);
      pie.render();

      const document = new TextDocument(
        "#highlight-text",
        "/data/textCategories.json",
        800,
        600,
        true,
        true
      );
      document.render();

      const map = new Map2D("#map-2d", "/data/geo.json", 800, 600);
      map.render();

      const network = new Network2D(
        "#network-2d",
        "/data/nodesandedges.json",
        800,
        600,
        10
      );
      network.render();
    </script>
  </body>
</html>
