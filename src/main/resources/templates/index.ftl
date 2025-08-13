<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <title>${title}</title>

    <link href="/css/styles.css" rel="stylesheet" />
  </head>

  <body>
    <div class="container">
      <#list configs?eval_json as config>
        <div 
          class="card" 
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
        const config = configs.find((conf) => conf.id === item.dataset.id);
        const Visualization = getter[config.type];

        const anchor = item.querySelector(".anchor");
        const endpoint = config.endpoint;
        const options = config.options;

        new Visualization(anchor, endpoint, options).render();
      });
    </script>
  </body>
</html>
