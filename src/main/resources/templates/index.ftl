<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>${title}</title>

    <link rel="stylesheet" href="/css/variables.css" />
    <link rel="stylesheet" href="/css/global.css" />
    <link rel="stylesheet" href="/css/index.css" />
    <link rel="stylesheet" href="/css/controls.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.1/font/bootstrap-icons.min.css" />
  </head>
  
  <body>
    <#include "/components/modal.ftl">
    <#include "/components/index/file-input.ftl">

    <div class="dv-layout">
      <div class="dv-main-title">
        <h2>Dynamic Visualizations</h2>
        <h4>Create, select or delete pipelines here</h4>
      </div>

      <div class="dv-menu-container">
        <div class="dv-title">Select a pipeline</div>
        <div class="dv-menu">
          <#list pipelines?eval_json as pipeline>
            <div class="dv-btn dv-menu-item">
              <a
                class="dv-menu-link"
                title="Select pipeline"
                href="/pipeline/${pipeline}"
              >
                <i class="bi bi-clipboard-data"></i>
                <span>${pipeline}</span>
              </a>
              <button
                class="dv-btn-delete"
                title="Delete pipeline"
                data-dv-toggle="modal"
                data-pipeline="${pipeline}"
              >
                <i class="bi bi-trash"></i>
              </button>
            </div>
          </#list>

          <#if pipelines?eval_json?size != 0>
            <div class="dv-divider"></div>
          </#if>

          <div class="dv-menu-item">
            <a
              class="dv-btn dv-menu-link"
              title="Create pipeline"
              href="/editor"
            >
              <i class="bi bi-plus-lg"></i>
              <span>Create new pipeline</span>
            </a>
          </div>
        </div>

        <div class="dv-separator">OR</div>

        <div class="dv-title">Start with a json configuration</div>
        <@fileInput />
      </div>

      <@modal />
    </div>

    <script type="module">
      import Modal from "/js/utils/classes/Modal.js";
      import components from "/js/utils/modules/components.js";

      const modal = new Modal(document.querySelector(".dv-modal").parentElement);
      components.initModal(modal);
      components.initFileInput(modal);
    </script>
  </body>
</html>
