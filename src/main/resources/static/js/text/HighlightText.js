import D3Visualization from "../D3Visualization.js";
import { flatData } from "../utils/helper.js";
import ControlsHandler from "../utils/ControlsHandler.js";
import ExportHandler from "../utils/ExportHandler.js";

export default class HighlightText extends D3Visualization {
  constructor(root, endpoint, { width, height }) {
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
    // Add controls
    for (const item of data.datasets) {
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
      this.init(data);
    }

    const spans = this.generateSpans(data);

    this.div
      .selectAll("span")
      .data(spans)
      .join("span")
      .text((d) => d.text)
      .attr("style", (item) => item.style)
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

    // Pass data to export handler
    this.exports.update(spans, null);
  }

  generateSpans({ text, datasets }) {
    // Split segments into begin and end events
    const events = this.splitSegments(datasets);
    const result = [];

    let last = 0;
    let styles = [];
    let labels = [];

    // Add styles to events to generate the spans
    for (const event of events) {
      if (last < event.index) {
        result.push({
          text: text.slice(last, event.index),
          label: labels.join(" "),
          style: styles.join(" "),
        });
      }

      // Update the current styles
      if (styles.find((item) => item === event.style)) {
        styles = styles.filter((item) => item !== event.style);
      } else {
        styles.push(event.style);
      }

      // Update the current labels
      if (labels.find((item) => item === event.label)) {
        labels = labels.filter((item) => item !== event.label);
      } else {
        labels.push(event.label);
      }

      last = event.index;
    }

    // Add optional last element without styles
    if (last < text.length) {
      result.push({
        text: text.slice(last),
      });
    }

    return result;
  }

  splitSegments(datasets) {
    const events = flatData(datasets, "segments").flatMap((segment) => {
      return [
        {
          index: segment.begin,
          label: `<span style="color: ${segment.color};">${segment.label}</span>`,
          style: this.getStyle(segment),
        },
        {
          index: segment.end,
          label: `<span style="color: ${segment.color};">${segment.label}</span>`,
          style: this.getStyle(segment),
        },
      ];
    });

    return events.sort((a, b) => a.index - b.index);
  }

  getStyle(item) {
    return {
      bold: `color: ${item.color}; font-weight: bold;`,
      underline: `text-decoration: underline 2px ${item.color};`,
      highlight: `background-color: ${item.color};`,
    }[item.style];
  }
}
