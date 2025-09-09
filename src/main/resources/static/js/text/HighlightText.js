import D3Visualization from "../D3Visualization.js";
import ControlsHandler from "../utils/ControlsHandler.js";
import ExportHandler from "../utils/ExportHandler.js";

export default class HighlightText extends D3Visualization {
    constructor(root, endpoint, {width, height}) {
        super(
            root,
            endpoint,
            {top: 0, right: 0, bottom: 0, left: 0},
            width,
            height
        );
        this.controls = new ControlsHandler(this.root.select(".dv-sidepanel-body"));
        this.exports = new ExportHandler(this.root.select(".dv-dropdown"), ["csv", "json"]);

        this.svg.remove();
        this.div = this.root
            .select(".dv-chart-area")
            .append("div")
            .style("width", this.width + "px")
            .style("height", this.height + "px")
            .style("padding", "1rem")
            .style("overflow-y", "auto");

        this.enabledTypes = new Set();
        this._initialized = false;

        // ensure filter object exists for D3Visualization.fetch()
        if (!this.filter) this.filter = {};
    }

    // drop-in shim to avoid base-class dependency
    setFilter(key, value) {
        if (!this.filter) this.filter = {};
        // keep empty string so the query is "?types=" when toggles are all off
        this.filter[key] = value == null ? "" : String(value);
    }

    init(data) {
        const datasets = Array.isArray(data?.datasets) ? data.datasets : [];
        this.enabledTypes = new Set(datasets.map((d) => d.name));
        this.setFilter("types", [...this.enabledTypes].join(",")); // server toggle param

        for (const item of datasets) {
            this.controls.appendSwitch(item.name, (on) => {
                if (on) this.enabledTypes.add(item.name);
                else this.enabledTypes.delete(item.name);

                this.setFilter("types", [...this.enabledTypes].join(",")); // empty → server returns no segments
                this.fetch().then((d) => this.render(d));
            });
        }
    }

    async render(data) {
        this.clear();

        if (!data) {
            data = await this.fetch();
            if (!this._initialized) {
                this.init(data);
                this._initialized = true;
            }
        }

        const spans = Array.isArray(data?.spans) ? data.spans : [];

        this.div
            .selectAll("span")
            .data(spans)
            .join("span")
            .text((d) => d.text)
            .attr("style", (d) => (d.style ? d.style : null))
            .filter((d) => d.label)
            .on("mouseover", (event) => this.mouseover(event.currentTarget))
            .on("mousemove", (event, d) =>
                this.mousemove(event.pageY, event.pageX + 20, `<strong>${d.label}</strong>`)
            )
            .on("mouseleave", (event) => this.mouseleave(event.currentTarget));

        this.exports.update(this.filter, spans, null);
    }
}
