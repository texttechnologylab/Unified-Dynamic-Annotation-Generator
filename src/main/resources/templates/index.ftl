<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>${title}</title>

    <link href="/css/styles.css" rel="stylesheet" />
    <link 
      href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css" 
      rel="stylesheet" 
      integrity="sha384-LN+7fdVzj6u52u30Kp6M/trliBMCMKTyK833zpbD+pXdCLuTusPj697FH4R/5mcr" 
      crossorigin="anonymous"
    >
  </head>

  <body>
    <div class="container">
      <#list configs?eval_json as config>
        <div 
          class="demo-card" 
          data-id="${config.id}"
        >
          <h2>${config.title}</h2>
          <div class="anchor"></div>
        </div>
      </#list>
    </div>

    <script type="module">
      import getter from "/js/utils/getter.js";

      const configs = JSON.parse("${configs?json_string}");

      document.querySelectorAll("[data-id]").forEach((item) => {
        const id = item.dataset.id;
        const config = configs.find((conf) => conf.id === id);
        const Visualization = getter[config.type];

        const anchor = item.querySelector(".anchor");
        const options = config.options;

        new Visualization(anchor, id, options).render();
      });
    </script>
  </body>
</html>
