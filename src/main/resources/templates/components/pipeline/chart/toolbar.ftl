<#macro toolbar id title>
  <div class="dv-toolbar">
    <button
      class="dv-btn dv-btn-toolbar"
      type="button"
      title="Controls"
      data-dv-toggle="sidepanel"
      data-dv-target="#${id}-sidepanel"
    >
      <i class="bi bi-sliders"></i>
    </button>

    <span class="dv-title dv-bold">${title}</span>

    <button
      class="dv-btn dv-btn-toolbar"
      type="button"
      title="Exports"
      data-bs-toggle="dropdown"
    >
      <i class="bi bi-download"></i>
    </button>
    <div class="dropdown-menu">
      <div class="dv-dropdown"></div>
    </div>
  </div>
</#macro>
