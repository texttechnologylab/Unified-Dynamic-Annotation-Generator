import D3Visualization from "../D3Visualization.js";
import ControlsHandler from "../../utils/classes/ControlsHandler.js";
import ExportHandler from "../../utils/classes/ExportHandler.js";

export default class HighlightText extends D3Visualization {
  constructor(root, endpoint, { width = 800, height = 600 }) {
    super(
      root,
      endpoint,
      { top: 0, right: 0, bottom: 0, left: 0 },
      width,
      height
    );
    this.controls = new ControlsHandler(this.root.select(".dv-sidepanel-body"));
    this.exports = new ExportHandler(this.root.select(".dv-dropdown"), [
      "csv",
      "json",
    ]);

    this.svg.remove();
    this.div = this.root
      .select(".dv-chart-area")
      .append("div")
      .style("width", this.width + "px")
      .style("height", this.height + "px")
      .style("padding", "1rem")
      .style("overflow-y", "auto");
  }

  init(data) {
    for (const item of data) {
      this.controls.appendSwitch(item.name, (value) => {
        console.log(value);
        this.fetch().then((data) => this.render(data));
      });
    }
  }

  async render(data) {
    this.clear();

    if (!data) {
      data = await this.fetch();
      this.init(data.datasets);
    }

    this.div
      .selectAll("span")
      .data(data.spans)
      .join("span")
      .text((item) => item.text)
      .attr("style", (d) => (d.style ? d.style : null))
      .filter((item) => item.label)
      .on("mouseover", (event) => this.mouseover(event.currentTarget))
      .on("mousemove", (event, data) =>
        this.mousemove(
          event.pageY,
          event.pageX + 20,
          `<strong>${data.label}</strong>`
        )
      )
      .on("mouseleave", (event) => this.mouseleave(event.currentTarget));

    this.exports.update(this.filter, data.spans, null);
  }
}
