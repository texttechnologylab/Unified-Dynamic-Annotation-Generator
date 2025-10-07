import { GridStack } from "https://cdn.jsdelivr.net/npm/gridstack@12.3.3/+esm";
import getter from "./getter.js";
import { corpusFilter } from "./filter/CorpusFilter.js";
import sidepanels from "../../shared/modules/sidepanels.js";
import accordions from "../../shared/modules/accordions.js";
import dropdowns from "../../shared/modules/dropdowns.js";

export default class View {
  constructor() {
    corpusFilter.init();
    sidepanels.init();
    accordions.init();
    dropdowns.init();

    const dropdown = document.querySelector(".dv-dropdown");
    const trigger = document.querySelector(".dv-pipeline-switcher-trigger");
    trigger.addEventListener("click", () => {
      dropdown.classList.toggle("show");
    });
    document.addEventListener("click", (event) => {
      if (!dropdown.contains(event.target) && !trigger.contains(event.target)) {
        dropdown.classList.remove("show");
      }
    });
  }

  initGrid(widgets) {
    const grid = GridStack.init({
      animate: false,
      float: true,
      disableDrag: true,
      disableResize: true,
    });

    grid.load(widgets);
  }

  initWidgets(widgets) {
    document.querySelectorAll("[data-dv-widget]").forEach((node) => {
      const id = node.dataset.dvWidget;
      const config = widgets.find((conf) => conf.id === id);

      const ChartClass = getter[config.type];

      if (ChartClass) {
        const endpoint = window.location.origin + "/api/data?id=" + id;
        const options = { ...config.options, ...this.getDimensions(node) };

        new ChartClass(node, endpoint, options).render();
      } else {
        node.classList.remove("hide");
      }
    });
  }

  getDimensions(element) {
    const area = element.querySelector(".dv-chart-area");
    const rect = area.getBoundingClientRect();

    return { width: rect.width, height: rect.height };
  }
}
