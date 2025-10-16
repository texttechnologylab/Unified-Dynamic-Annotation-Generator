<#macro grid widgets>
  <div class="grid-stack">
    <#list widgets as widget>
      <div class="grid-stack-item" gs-id="${widget.id}">
        
        <#if widget.type == "StaticText">
          <div class="grid-stack-item-content hide">
            <button
              class="dv-btn-autohide"
              type="button"
              title="Edit"
            >
              <i class="bi bi-pencil"></i>
            </button>
            <div id="static-text"></div>
            <button
              class="dv-btn-autohide"
              type="button"
              title="Remove"
            >
              <i class="bi bi-x-lg"></i>
            </button>
          </div>

        <#elseif widget.type == "StaticImage">
          <div 
            class="grid-stack-item-content hide" 
          >
            <button
              class="dv-btn-autohide"
              type="button"
              title="Edit"
            >
              <i class="bi bi-pencil"></i>
            </button>
            <img 
              width="100%" 
              height="100%" 
            >
            <button
              class="dv-btn-autohide"
              type="button"
              title="Remove"
            >
              <i class="bi bi-x-lg"></i>
            </button>
          </div>

        <#else>
          <div class="grid-stack-item-content dv-chart hide">
            <div class="dv-toolbar">
              <button
                class="dv-btn dv-btn-toolbar"
                type="button"
                title="Edit"
              >
                <i class="bi bi-pencil"></i>
              </button>

              <span class="dv-toolbar-title">
              </span>

              <button
                class="dv-btn dv-btn-toolbar"
                type="button"
                title="Remove"
              >
                <i class="bi bi-x-lg"></i>
              </button>
            </div>
            <div class="dv-chart-area dv-chart-icon">
              <i></i>
            </div>
          </div>
        </#if>

      </div>
    </#list>
  </div>

  <template id="static-text-template">
    <div class="grid-stack-item-content">
      <button
        class="dv-btn-autohide"
        type="button"
        title="Edit"
      >
        <i class="bi bi-pencil"></i>
      </button>
      <div id="static-text"></div>
      <button
        class="dv-btn-autohide"
        type="button"
        title="Remove"
      >
        <i class="bi bi-x-lg"></i>
      </button>
    </div>
  </template>

  <template id="static-image-template">
    <div class="grid-stack-item-content">
      <button
        class="dv-btn-autohide"
        type="button"
        title="Edit"
      >
        <i class="bi bi-pencil"></i>
      </button>
      <img
        width="100%"
        height="100%"
      />
      <button
        class="dv-btn-autohide"
        type="button"
        title="Remove"
      >
        <i class="bi bi-x-lg"></i>
      </button>
    </div>
  </template>

  <template id="default-chart-template">
    <div class="grid-stack-item-content dv-chart">
      <div class="dv-toolbar">
        <button
          class="dv-btn dv-btn-toolbar"
          type="button"
          title="Edit"
        >
          <i class="bi bi-pencil"></i>
        </button>

        <span class="dv-toolbar-title"></span>

        <button
          class="dv-btn dv-btn-toolbar"
          type="button"
          title="Remove"
        >
          <i class="bi bi-x-lg"></i>
        </button>
      </div>
      <div class="dv-chart-area dv-chart-icon">
        <i></i>
      </div>
    </div>
  </template>
</#macro>
