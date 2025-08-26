<#macro toolbar config>
  <div class="dv-toolbar">
    <button
      class="dv-btn dv-title"
      type="button"
      data-toggle="sidepanel"
      data-target="#${config.id}-sidepanel"
    >
      <i class="bi bi-sliders"></i>
    </button>
    <span class="dv-title dv-bold">${config.title}</span>
    <div class="dropend">
      <button class="dv-btn dv-title" type="button" data-bs-toggle="dropdown">
        <i class="bi bi-download"></i>
      </button>
      <div class="dropdown-menu shadow">
        <button class="dv-btn" type="button">
          <i class="bi bi-image"></i>
          Export as png
        </button>
        <button class="dv-btn" type="button">
          <i class="bi bi-braces"></i>
          Export as json
        </button>
        <button class="dv-btn" type="button">
          <i class="bi bi-table"></i>
          Export as csv
        </button>
      </div>
    </div>
  </div>
</#macro>
