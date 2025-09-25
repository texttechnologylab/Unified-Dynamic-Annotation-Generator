<#macro accordion icon title>
  <button
    class="dv-btn dv-btn-accordion"
    type="button"
    data-dv-toggle="accordion"
    data-dv-target="#${title}-accordion"
  >
    <div class="dv-accordion-title">
      <i class="${icon}"></i>
      <span>${title}</span>
    </div>
    <i class="bi bi-chevron-down"></i>
  </button>
  <div 
    id="${title}-accordion"
    class="collapse" 
  >
    <div class="dv-accordion-body">
      <#nested>
    </div>
  </div>
</#macro>
