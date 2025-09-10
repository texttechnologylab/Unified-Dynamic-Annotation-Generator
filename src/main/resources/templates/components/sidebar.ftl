<#include "/components/accordion.ftl">

<#macro sidebar filters>
  <aside class="dv-sidebar">
    <div class="dv-sidebar-header">
      <h1 class="dv-bold">Dynamic Visualizations</h1>
      <h4>Corpus Filter</h4>
    </div>

    <div class="dv-sidebar-body">
      <@accordion icon="bi bi-file-earmark-text-fill" title="Files">
          <div class="dv-file-filter">
            <input
              type="text"
              class="dv-text-input"
              placeholder="Type to search"
            >

            <div class="dv-filter-checkboxes">
              <label class="dv-checkbox-label" title="All">
                <input
                  type="checkbox"
                  class="dv-checkbox-all form-check-input"
                  value="all"
                  checked
                >
                <span>All</span>
              </label>
              <div class="dv-divider"></div>
              <#list filters.files as file>
                <label class="dv-checkbox-label" title="${file.name}">
                  <input
                    type="checkbox"
                    class="dv-checkbox form-check-input"
                    value="${file.id}"
                    checked
                  >
                  <span>${file.name}</span>
                </label>
              </#list>
            </div>
            <div class="dv-divider"></div>

            <div class="dv-selection-info"></div>
          </div>
      </@accordion>

      <#if filters.date??>
        <@accordion icon="bi bi-calendar-fill" title="Date">
            <div class="dv-date-filter">
              <div>
                <span class="">min</span>
                <input 
                  type="date" 
                  class="dv-date-input" 
                  min="${filters.date.min}" 
                  max="${filters.date.max}"
                >
              </div>
              <div>
                <span class="">max</span>
                <input 
                  type="date" 
                  class="dv-date-input" 
                  min="${filters.date.min}" 
                  max="${filters.date.max}"
                >
              </div>
            </div>
        </@accordion>
      </#if>
    </div>
  </aside>
</#macro>
