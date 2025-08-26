<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>${title}</title>

    <link href="/css/index.css" rel="stylesheet" />
    <link href="/css/chart.css" rel="stylesheet" />
    <link
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css"
      rel="stylesheet"
      integrity="sha384-LN+7fdVzj6u52u30Kp6M/trliBMCMKTyK833zpbD+pXdCLuTusPj697FH4R/5mcr"
      crossorigin="anonymous"
    />
    <link
      rel="stylesheet"
      href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.1/font/bootstrap-icons.min.css"
    />
  </head>

  <body>
    <#include "/components/toolbar.ftl">
    <#include "/components/sidepanel.ftl">

    <div class="container">
      <#list configs?eval_json as config>
        <div class="dv-card">

          <div class="dv-chart" data-id="${config.id}">
            <@toolbar config=config />

            <div class="dv-chart-area">
              <@sidepanel config=config />
            </div>

            <div class="dv-tooltip"></div>
          </div>
          
        </div>
      </#list>
    </div>

    <script type="module">
      import getter from "/js/utils/getter.js";
      import sidepanels from "/js/utils/sidepanels.js";

      const configs = JSON.parse("${configs?json_string}");

      document.querySelectorAll("[data-id]").forEach((item) => {
        const id = item.dataset.id;
        const config = configs.find((conf) => conf.id === id);

        const ChartClass = getter[config.type];
        const endpoint = window.location.origin + "/api/data?type=any&id=" + id;

        new ChartClass(item, endpoint, config.options).render();
      });
      sidepanels.init();
    </script>
    <script
      src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/js/bootstrap.bundle.min.js"
      integrity="sha384-ndDqU0Gzau9qJ1lfW4pNLlhNTkCfHzAVBReH9diLvGRem5+R9g2FzA8ZGN954O5Q"
      crossorigin="anonymous"
    ></script>
  </body>
</html>
