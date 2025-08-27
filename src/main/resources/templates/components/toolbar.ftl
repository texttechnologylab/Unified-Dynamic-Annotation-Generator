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
      <div class="dropdown-menu dv-dropdown-menu"></div>
    </div>
  </div>
</#macro>
