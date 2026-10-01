"""Convert source MCX rectangles to native faces without losing positions or UVs.
The 22.5-degree filter is inverse-rotated before conversion. Assert instead of
silently approximating unsupported geometry. Run from the repository root.
"""
import argparse
import json
import math
import pathlib

CORNERS = {
    "down": ((0,0,1),(0,0,0),(1,0,0),(1,0,1)),
    "up": ((0,1,0),(0,1,1),(1,1,1),(1,1,0)),
    "north": ((1,1,0),(1,0,0),(0,0,0),(0,1,0)),
    "south": ((0,1,1),(0,0,1),(1,0,1),(1,1,1)),
    "west": ((0,1,0),(0,0,0),(0,0,1),(0,1,1)),
    "east": ((1,1,1),(1,0,1),(1,0,0),(1,1,0)),
}
def rotate(p, angle):
    c, s = math.cos(math.radians(angle)), math.sin(math.radians(angle))
    return (p[0], p[1]*c-p[2]*s, p[1]*s+p[2]*c)

def close(a, b):
    return all(abs(x-y) < 1e-6 for x,y in zip(a,b))

def convert(source, water=False, gravel=None):
    elements = []
    textures = {"particle": "magneticraft:block/table_sieve_side"}
    for idx, (positions, texcoords) in enumerate(source["quads"]["indices"]):
        if gravel is not None and not 53 <= idx < 59:
            continue
        points = [source["quads"]["pos"][i] for i in positions]
        shifted = 4/16 * gravel/10 if gravel is not None else 0
        original = [(p[0],p[1]+shifted,p[2]) for p in points]
        points = original
        angle = -22.5 if (water and 16 <= idx < 22) or (not water and 59 <= idx < 65) else 0
        if angle:
            points = [rotate(p, -angle) for p in points]
        lo = [min(p[a] for p in points) for a in range(3)]
        hi = [max(p[a] for p in points) for a in range(3)]
        axes = [a for a in range(3) if abs(hi[a]-lo[a]) < 1e-6]
        assert len(axes) == 1, ("Unsupported quad", idx, points)
        a = [points[1][i]-points[0][i] for i in range(3)]
        b = [points[2][i]-points[0][i] for i in range(3)]
        normal = (a[1]*b[2]-a[2]*b[1],a[2]*b[0]-a[0]*b[2],a[0]*b[1]-a[1]*b[0])
        axis = axes[0]
        face = (("west","east"),("down","up"),("north","south"))[axis][normal[axis]>0]
        canonical = [[(hi if bit else lo)[i] for i,bit in enumerate(c)] for c in CORNERS[face]]
        order = []
        for p in canonical:
            matches = [i for i,q in enumerate(points) if close(p,q)]
            assert len(matches) == 1, ("Non-rectangular quad", idx)
            order.append(matches[0])
        uv = [source["quads"]["tex"][texcoords[i]] for i in order]
        selected = None
        for rotation in (0,90,180,270):
            indices = [((v+rotation//90)%4) for v in range(4)]
            corners = [None]*4
            for v in range(4): corners[indices[v]] = uv[v]
            u0,v0 = corners[0]; u1,v1 = corners[2]
            expected = ((u0,v0),(u0,v1),(u1,v1),(u1,v0))
            if all(close(expected[i],corners[i]) for i in range(4)):
                selected = {"uv":[round(x*16,6) for x in (u0,v0,u1,v1)],"rotation":rotation}
                break
        assert selected is not None, ("Unsupported UV", idx, uv)
        part = next(p for p in source["parts"] if p["from"] <= idx < p["to"] and
                    (p["name"] == "gravel" if gravel is not None else p["name"] != "gravel"))
        name = "water" if water else part["texture"].split("/")[-1]
        textures[name] = "minecraft:block/water_flow" if water else "magneticraft:block/"+name
        selected["texture"] = "#"+name
        element = {"from":[round(v*16,6) for v in lo],
                   "to":[round(v*16,6) for v in hi],"faces":{face:selected}}
        assert all(-16 <= v <= 32 for v in element["from"]+element["to"])
        if angle: element["rotation"] = {"origin":[0,0,0],"axis":"x","angle":angle,"rescale":False}
        # Reconstruct vertices, ensuring the conversion is geometrically faithful.
        reconstructed = [rotate(p,angle) if angle else p for p in canonical]
        assert all(any(close(p,q) for q in reconstructed) for p in original), idx
        elements.append(element)
    model = {"parent":"minecraft:block/block","ambientocclusion":not water,"textures":textures,"elements":elements}
    if water: model["render_type"] = "minecraft:translucent"
    return model

def build():
    legacy = pathlib.Path("src/main/resources/assets/magneticraft/models/block/mcx")
    source = json.loads((legacy/"sluice_box.mcx").read_text())
    water = json.loads((legacy/"sluice_box_water.mcx").read_text())
    models = {"sluice_box":convert(source),
            "sluice_box_water":convert(water,water=True),
            **{f"sluice_box_gravel_{i}":convert(source,gravel=i) for i in range(1,11)}}
    props = pathlib.Path("gradle.properties").read_text()
    if "minecraft_version=26.3" in props:
        model = models["sluice_box_water"]
        model.pop("render_type")
        model["textures"] = {key: {"sprite": value, "force_translucent": True} for key, value in model["textures"].items()}
    return models

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--check",action="store_true")
    args = parser.parse_args()
    root = pathlib.Path("port/src/main/resources/assets/magneticraft/models/block")
    for name,model in build().items():
        path = root/(name+".json")
        if args.check:
            assert json.loads(path.read_text()) == model, f"Model diverged from source: {name}"
        else:
            root.mkdir(parents=True,exist_ok=True)
            path.write_text(json.dumps(model,indent=2)+"\n")
    print("Sluice source positions, faces, UVs, rotation and model parity checks passed")

if __name__ == "__main__":
    main()
