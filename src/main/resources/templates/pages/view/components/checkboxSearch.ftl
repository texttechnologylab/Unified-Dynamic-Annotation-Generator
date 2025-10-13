<#macro checkboxSearch id total>
  <div id="${id}" data-dv-filter="checkbox">
    <div class="dv-dropdown-container">
      <input
        class="dv-autocomplete-input"
        type="text"
        placeholder="Type to search"
      />
      <div class="dv-dropdown"></div>
    </div>

    <div class="dv-filter-checkboxes"></div>
  </div>
</#macro>

<template id="result-template">
  <button class="dv-btn dv-autocomplete-result" type="button">
    <span></span>
    <i class="bi bi-plus-lg"></i>
  </button>
</template>

<template id="checkbox-template">
  <label class="dv-filter-checkbox">
    <div class="dv-checkbox-container">
      <input
        class="dv-check-input form-check-input"
        type="checkbox"
        checked
      />
      <span class="dv-text-truncate"></span>
    </div>
    <button class="dv-btn-delete" type="button">
      <i class="bi bi-x-lg"></i>
    </button>
  </label>
</template>
