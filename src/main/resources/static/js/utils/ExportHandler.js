export default class ExportHandler {
  constructor(node, formats) {
    this.serializer = new XMLSerializer();
    this.storage = {
      json: null,
      svg: null,
    };

    const icons = {
      svg: "bi bi-file-earmark-code",
      png: "bi bi-image",
      json: "bi bi-braces",
      csv: "bi bi-table",
    };

    formats.forEach((format) => {
      const btn = node
        .append("button")
        .attr("class", "dv-btn")
        .attr("type", "button")
        .on("click", () => this.prepareExport(format));

      btn.append("i").attr("class", icons[format]);
      btn.append("span").text("Export as " + format);
    });
  }

  update(json, svg) {
    this.storage.json = json;
    this.storage.svg = svg;
  }

  prepareExport(format) {
    switch (format) {
      case "svg":
        const string = this.serializer.serializeToString(this.storage.svg);
        this.exportAsFile(
          `<?xml version="1.0" standalone="no"?>\r\n${string}`,
          "image/svg+xml",
          "chart.svg"
        );
        break;

      case "png":
        break;

      case "csv":
        break;

      default:
        this.exportAsFile(
          JSON.stringify(this.storage.json, null, 2),
          "text/json",
          "data.json"
        );
        break;
    }
  }

  exportAsFile(content, type, name) {
    const a = document.createElement("a");
    a.href = URL.createObjectURL(new Blob([content], { type }));
    a.download = name;

    a.click();
    a.remove();
  }
}
