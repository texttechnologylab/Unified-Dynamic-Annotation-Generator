<#include "/components/accordion.ftl">

<#macro sidebar pipeline filters>
  <aside class="dv-sidebar">
    <div class="dv-sidebar-header">
      <h1 class="dv-bold">Dynamic Visualizations</h1>
    </div>

    <div class="dv-sidebar-body">
      <a 
        class="dv-btn dv-menu-link"
        href="/"
      >
        <i class="bi bi-box-arrow-in-left"></i>
        <span>Pipeline selection</span>
      </a>
      
      <div class="dv-menu-title">Pipeline: ${pipeline}</div>
      <a 
        class="dv-btn dv-menu-link"
        href="/editor/${pipeline}"
      >
        <i class="bi bi-pencil"></i>
        <span>Edit configuration</span>
      </a>
      <a 
        class="dv-btn dv-menu-link"
        href="/api/visualisations?pipelineId=${pipeline}&pretty=true"
        download="pipeline.json"
      >
        <i class="bi bi-download"></i>
        <span>Export configuration</span>
      </a>
      
      <div class="dv-menu-title">Corpus Filter</div>
      <@accordion icon="bi bi-file-earmark-text" title="Files">
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

      <#if filters.tags??>
        <@accordion icon="bi bi-tags" title="Tags">
        </@accordion>
      </#if>

      <#if filters.date??>
        <@accordion icon="bi bi-calendar-week" title="Date">
            <div class="dv-date-filter">
              <div>
                <span class="">min</span>
                <input 
                  type="date" 
                  class="dv-date-input" 
                  min="${filters.date.min}" 
                  max="${filters.date.max}"
                  required
                >
              </div>
              <div>
                <span class="">max</span>
                <input 
                  type="date" 
                  class="dv-date-input" 
                  min="${filters.date.min}" 
                  max="${filters.date.max}"
                  required
                >
              </div>
            </div>
        </@accordion>
      </#if>

      <div class="d-flex justify-content-center mt-2">
        <button
          id="btn-apply-filter"
          type="button"
          class="dv-btn-outline"
        >
          Apply filter
        </button>
      </div>
    </div>
  </aside>
</#macro>
