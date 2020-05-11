import matplotlib

matplotlib.use('TkAgg')
import re
from argparse import ArgumentParser
from collections import defaultdict

import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D


class Item(object):
    def __init__(self):
        self.filename = "unknown"
        self.findings = defaultdict(int)
        self.size = -1
        self.size_human = -1
        self.duration = -1
        self.duration_human = -1
        self.error = False
        self.timeout = False

    def __str__(self):
        return f"File: {self.filename}, " \
               f"findings: {self.findings}, " \
               f"size: {self.size}"

    __repr__ = __str__


def main(report):
    with open(report) as f:
        duration_pattern = re.compile(
          r"(?P<duration>\d+) ms( \((?P<human>.+)\))?")
        filename_pattern = re.compile(
          r"https://raw\.githubusercontent\.com/(?P<repo>[^/]*/[^/]*)/[^/]*/(?P<path>.*)\n")
        size_pattern = re.compile(r"(?P<size>\d+) B( \((?P<human>.+)\))?")
        items = defaultdict(lambda: Item())
        verified_orders = 0
        violations = 0
        curr_object = None
        for line in f:
            if line.startswith("\t\t\tline"):
                if "Verified Order" in line:
                    finding = "verified order"
                    verified_orders += 1
                else:
                    finding = "violation"
                    violations += 1
                items[curr_object].findings[finding] += 1
            elif line.startswith("\t"):
                if "Timeout" in line:
                    items[curr_object].timeout = True
                elif "Error" in line:
                    items[curr_object].error = True
                elif "Duration" in line:
                    match = duration_pattern.search(line)
                    if match:
                        duration = int(match.group("duration"))
                        items[curr_object].duration = duration
                        human = match.group("human")
                        if human:
                            items[curr_object].duration_human = human
                        else:
                            items[curr_object].duration_human = f"{duration} ms"
                elif "Size" in line:
                    match = size_pattern.search(line)
                    if match:
                        size = int(match.group("size"))
                        items[curr_object].size = size
                        human = match.group("human")
                        if human:
                            items[curr_object].size_human = human
                        else:
                            items[curr_object].size_human = f"{size} B"

            elif line.startswith("http"):
                curr_object = line
                match = filename_pattern.search(line)
                if match:
                    items[
                        curr_object].filename = f"{match.group('repo')}/{match.group('path')}"

        print(f"Parsed {len(items.values())} items")
        show_plot(items, violations, verified_orders)
        show_plot_3d(items, violations, verified_orders)
        plt.show()


def get_color(item):
    if item.filename.endswith("java"):
        return "red" if item.error else "green"
    elif item.filename.endswith("cpp"):
        return "orange" if item.error else "blue"
    else:
        return "black" if item.error else "lightgray"


def show_plot(items, violations, verified_orders):
    sizes, durations, colors = zip(
      *[(i.size, i.duration, get_color(i)) for i in
        items.values()])
    fig, ax = plt.subplots()
    plt.title(f"{len(items)} files analyzed, "
              f"{violations} violation{'' if violations == 1 else 's'} and "
              f"{verified_orders} verified order{'' if violations == 1 else 's'}")
    plt.xlabel("Size (B)")
    plt.ylabel("Duration (ms)")
    sc = plt.scatter(x=sizes, y=durations, c=colors)
    annot = ax.annotate("", xy=(0, 0), xytext=(-0.1, 1.05),
                        textcoords="axes fraction",
                        bbox=dict(boxstyle="round", fc="w"),
                        arrowprops=dict(arrowstyle="->"))

    def update_annot(ind):

        pos = sc.get_offsets()[ind["ind"][0]]
        annot.xy = pos
        size, duration = pos
        matches = filter(lambda i: i.size == size and i.duration == duration,
                         items.values())
        if matches:
            item = next(matches)
            findings = ", ".join([f"{finding}: {count}x" for finding, count in
                                  item.findings.items()])
            findings = findings or "No findings"
            text = f"{'ERROR' if item.error else 'Success'}\n" \
                   f"{item.filename}\n" \
                   f"size: {item.size_human}, duration: {item.duration_human}\n" \
                   f"{findings}"
        else:
            text = "unknown item"
        annot.set_text(text)
        annot.get_bbox_patch().set_alpha(0.4)

    def hover(event):
        vis = annot.get_visible()
        if event.inaxes == ax:
            cont, ind = sc.contains(event)
            if cont:
                update_annot(ind)
                annot.set_visible(True)
                fig.canvas.draw_idle()
            else:
                if vis:
                    annot.set_visible(False)
                    fig.canvas.draw_idle()

    fig.canvas.mpl_connect("motion_notify_event", hover)


def show_plot_3d(items, violations, verified_orders):
    sizes, durations, findings, colors = zip(
      *[(i.size, i.duration, sum(i.findings.values()), get_color(i)) for i in
        items.values()])
    fig = plt.figure()
    ax = fig.add_subplot(111, projection='3d')
    ax.set_title(f"{len(items)} files analyzed, "
                 f"{violations} violation{'' if violations == 1 else 's'} and "
                 f"{verified_orders} verified order{'' if violations == 1 else 's'}\n"
                 f"(Point labels might be inaccurate due to incorrect 2D to 3D "
                 f"coordinate transformation!)", y=1.03)
    ax.set_xlabel("Size (B)")
    ax.set_ylabel("Duration (ms)")
    ax.set_zlabel("Findings")
    sc = ax.scatter(xs=sizes, ys=durations, zs=findings, c=colors)
    annot = ax.annotate("", xy=(0, 0), xytext=(-0.1, 1.04),
                        textcoords="axes fraction",
                        bbox=dict(boxstyle="round", fc="w"),
                        arrowprops=dict(arrowstyle="->"))

    coord_pattern = re.compile(
      r"x=(?P<x>[+\-\d.]+)[\s,]*y=(?P<y>[+\-\d.]+)[\s,]*z=(?P<z>[+\-\d.]+)")

    def rel_error(correct, estimate):
        if correct == 0:
            return estimate
        return abs((correct - estimate) / correct)

    def update_annot(ind, x, y, z):
        pos = sc.get_offsets()[ind["ind"][0]]
        annot.xy = pos
        distances = [(rel_error(sum(i.findings.values()), z),
                      rel_error(i.duration, y),
                      rel_error(i.size, x),
                      i)
                     for i in items.values()]
        matches = sorted(
          filter(lambda d: True,
                 distances), key=lambda d: sum(d[:3]))
        if matches:
            item = matches[0][3]
            curr_findings = ", ".join(
              [f"{finding}: {count}x" for finding, count in
               item.findings.items()])
            curr_findings = curr_findings or "No findings"
            text = f"{'ERROR' if item.error else 'Success'}\n" \
                   f"{item.filename}\n" \
                   f"size: {item.size_human}, " \
                   f"duration: {item.duration_human}\n" \
                   f"{curr_findings}"
        else:
            text = "unknown item"
        annot.set_text(text)
        annot.get_bbox_patch().set_alpha(0.4)

    def hover(event):
        vis = annot.get_visible()
        if event.inaxes == ax:
            cont, ind = sc.contains(event)
            if cont:
                guess = ax.format_coord(event.xdata, event.ydata)
                m = coord_pattern.match(guess)
                x, y, z = map(float, (m.group("x"), m.group("y"), m.group("z")))
                update_annot(ind, x, y, z)
                annot.set_visible(True)
                fig.canvas.draw_idle()
            else:
                if vis:
                    annot.set_visible(False)
                    fig.canvas.draw_idle()

    fig.canvas.mpl_connect("motion_notify_event", hover)


if __name__ == '__main__':
    parser = ArgumentParser(
      description="Analyze GitHub crypto analyzer results")
    parser.add_argument("report",
                        help="The report file produced by the crypto analyzer")
    options = parser.parse_args()
    main(options.report)
