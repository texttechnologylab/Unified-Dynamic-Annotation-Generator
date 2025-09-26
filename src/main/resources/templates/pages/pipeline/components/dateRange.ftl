<#macro dateRange id min max>
  <div id="${id}" class="dv-filter-date-inputs" data-dv-filter="date">
    <div>
      <span>min</span>
      <input
        type="date"
        class="dv-date-input"
        min="${min}"
        max="${max}"
        required
      />
    </div>
    <div>
      <span>max</span>
      <input
        type="date"
        class="dv-date-input"
        min="${min}"
        max="${max}"
        required
      />
    </div>
  </div>
</#macro>
