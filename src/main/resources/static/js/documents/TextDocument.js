import * as d3 from "https://cdn.jsdelivr.net/npm/d3@7/+esm";
import D3Visualization from "../D3Visualization.js";

export default class HighlightText extends D3Visualization {
  constructor(anchor, endpoint, { width, height }) {
    super(
      anchor,
      endpoint,
      { top: 0, right: 0, bottom: 0, left: 0 },
      width,
      height
    );

    d3.select(this.anchor).select("svg").remove();
    this.div = d3
      .select(this.anchor)
      .append("div")
      .style("width", this.width + "px")
      .style("height", this.height + "px")
      .style("text-align", "start")
      .style("line-height", "1.8rem")
      .style("overflow-y", "auto");
  }

  render() {
    this.fetch().then((data) => {
      data = this.generateSpans(data);

      this.div
        .selectAll("span")
        .data(data)
        .join("span")
        .text((d) => d.text)
        .attr("style", (item) => item.style)
        .filter((item) => item.label)
        .style("cursor", "pointer")
        .on("mouseover", this.mouseover)
        .on("mouseleave", this.mouseleave);
    });
  }

  generateSpans({ text, categories }) {
    const result = [];
    const events = [];

    categories
      .flatMap((item) => item.segments)
      .forEach((segment) => {
        const style = this.getStyle(segment);
        events.push({
          index: segment.begin,
          label: `<span style="color: ${segment.color};">${segment.label}</span>`,
          style,
        });
        events.push({
          index: segment.end,
          label: `<span style="color: ${segment.color};">${segment.label}</span>`,
          style,
        });
      });
    events.sort((a, b) => a.index - b.index);

    let last = 0;
    let styles = [];
    let labels = [];

    for (const event of events) {
      if (last < event.index) {
        result.push({
          text: text.slice(last, event.index),
          label: labels.join(" "),
          style: styles.join(" "),
        });
      }

      if (styles.find((item) => item === event.style)) {
        styles = styles.filter((item) => item !== event.style);
      } else {
        styles.push(event.style);
      }

      if (labels.find((item) => item === event.label)) {
        labels = labels.filter((item) => item !== event.label);
      } else {
        labels.push(event.label);
      }

      last = event.index;
    }

    if (last < text.length) {
      result.push({
        text: text.slice(last),
      });
    }

    return result;
  }

  getStyle(item) {
    return {
      bold: `color: ${item.color}; font-weight: bold;`,
      underline: `text-decoration: underline 2px ${item.color};`,
      highlight: `background-color: ${item.color};`,
    }[item.style];
  }

  mouseover = (event, data) => {
    const rect = event.target.getBoundingClientRect();

    this.tooltip
      .html(`<strong>${data.label}</strong>`)
      .style("left", rect.left + window.scrollX + "px")
      .style("top", rect.top + window.scrollY - rect.height * 1.5 + "px")
      .style("opacity", 1);

    d3.select(event.target).style("opacity", 0.8);
  };

  mouseleave = (event) => {
    this.tooltip.style("opacity", 0);

    d3.select(event.target).style("opacity", 1);
  };
}
