<#macro grid widgets>
  <div class="grid-stack">
    <#list widgets as widget>
      <div class="grid-stack-item" gs-id="${widget.id}">
        
        <#if widget.type == "StaticText">
          <div 
            class="grid-stack-item-content ${widget.options.style} hide" 
          >
            ${widget.options.text}
          </div>

        <#elseif widget.type == "StaticImage">
          <div 
            class="grid-stack-item-content hide" 
          >
            <img 
              src="${widget.options.src}" 
              width="100%" 
              height="100%" 
            >
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
                ${widget.title}
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

  <template id="text-placeholder">
    <div class="grid-stack-item-content">
      The quick brown fox jumps over the lazy dog.
    </div>
  </template>

  <template id="image-placeholder">
    <div class="grid-stack-item-content">
      <img
        src="https://placehold.co/600x400?text=My+Logo"
        width="100%"
        height="100%"
      />
    </div>
  </template>

  <template id="d3-chart-placeholder">
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
