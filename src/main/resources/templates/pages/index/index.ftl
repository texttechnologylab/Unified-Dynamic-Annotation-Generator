<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1" />
    <title>Dynamic Visualizations</title>

    <link rel="stylesheet" href="/css/variables.css" />
    <link rel="stylesheet" href="/css/pages/index.css" />
    <link rel="stylesheet" href="/css/shared/globals.css" />
    <link rel="stylesheet" href="/css/shared/components.css" />
    <link rel="stylesheet" href="/css/shared/controls.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.7/dist/css/bootstrap.min.css" />
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.13.1/font/bootstrap-icons.min.css" />
  </head>
  
  <body>
    <#include "/shared/modal.ftl">
    <#include "/shared/fileInput.ftl">

    <div class="dv-layout">
      <div class="dv-main-title">
        <h2>Dynamic Visualizations</h2>
        <h4>Create, select or delete pipelines here</h4>
      </div>

      <div class="dv-menu-container">
        <div class="dv-title">Select a pipeline</div>
        <div class="dv-menu">
          <div class="dv-menu-item-list">
            <#list pipelines?eval_json as pipeline>
              <div class="dv-btn dv-menu-item">
                <a
                  class="dv-menu-link"
                  title="Select pipeline"
                  href="/view/${pipeline}"
                >
                  <i class="bi bi-clipboard-data"></i>
                  <span>${pipeline}</span>
                </a>

                <a
                  class="dv-btn-hidden"
                  title="Edit configuration"
                  href="/editor/${pipeline}"
                >
                  <i class="bi bi-pencil"></i>
                </a>
                <a
                  class="dv-btn-hidden"
                  title="Export configuration"
                  href="/api/pipelines/${pipeline}?pretty=true"
                  download="config.json"
                >
                  <i class="bi bi-download"></i>
                </a>
                <button
                  class="dv-btn-hidden dv-btn-delete"
                  title="Delete pipeline"
                  data-dv-toggle="modal"
                  data-pipeline="${pipeline}"
                >
                  <i class="bi bi-trash"></i>
                </button>
              </div>
            </#list>
          </div>

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
        <@fileInput info="Single file • JSON" accept="application/json" />
      </div>

      <@modal />
    </div>

    <script type="module">
      import Modal from "/js/shared/classes/Modal.js";
      import fileInput from "/js/shared/modules/fileInput.js";

      const modal = new Modal(document.querySelector(".dv-modal").parentElement);
      
      document.querySelectorAll("[data-dv-toggle='modal']").forEach((node) => {
        node.addEventListener("click", () => {
          modal.confirm(
            "Delete " + node.dataset.pipeline,
            "Do you want to delete this pipeline?",
            () => console.log(node.dataset.pipeline)
          );
        });
      });
      
      fileInput.init(modal);
    </script>
  </body>
</html>
