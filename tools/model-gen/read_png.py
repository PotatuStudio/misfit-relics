"""Lector mínimo de PNG (RGBA o con paleta) para las vistas previas: devuelve filas de tuplas (r, g, b, a)."""
import struct
import zlib


def read_rgba(path):
    d = open(path, 'rb').read()
    pos = 8
    idat = b''
    pal, trns = [], []
    w = h = bd = ct = 0
    while pos < len(d):
        length = struct.unpack('>I', d[pos:pos + 4])[0]
        tag, chunk = d[pos + 4:pos + 8], d[pos + 8:pos + 8 + length]
        if tag == b'IHDR':
            w, h, bd, ct = struct.unpack('>IIBB', chunk[:10])
        elif tag == b'IDAT':
            idat += chunk
        elif tag == b'PLTE':
            pal = [tuple(chunk[i:i + 3]) for i in range(0, len(chunk), 3)]
        elif tag == b'tRNS':
            trns = list(chunk)
        pos += 12 + length
    raw = zlib.decompress(idat)
    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[ct]
    bpp = max(1, channels * bd // 8)
    stride = (w * bpp * bd + 7) // 8
    rows, prev, i = [], bytearray(stride), 0
    for _ in range(h):
        f, line = raw[i], bytearray(raw[i + 1:i + 1 + stride])
        i += 1 + stride
        for x in range(stride):
            a = line[x - bpp] if x >= bpp else 0
            b = prev[x]
            c = prev[x - bpp] if x >= bpp else 0
            if f == 1:
                line[x] = (line[x] + a) & 255
            elif f == 2:
                line[x] = (line[x] + b) & 255
            elif f == 3:
                line[x] = (line[x] + (a + b) // 2) & 255
            elif f == 4:
                p = a + b - c
                pa, pb, pc = abs(p - a), abs(p - b), abs(p - c)
                line[x] = (line[x] + (a if pa <= pb and pa <= pc else b if pb <= pc else c)) & 255
        rows.append(line)
        prev = line
    img = []
    for line in rows:
        r = []
        for x in range(w):
            if ct == 3:
                if bd == 8:
                    idx = line[x]
                else:
                    per = 8 // bd
                    idx = (line[x // per] >> (8 - bd * (x % per + 1))) & ((1 << bd) - 1)
                r.append(tuple(pal[idx]) + (trns[idx] if idx < len(trns) else 255,))
            elif ct == 6:
                r.append(tuple(line[x * 4:x * 4 + 4]))
            elif ct == 2:
                r.append(tuple(line[x * 3:x * 3 + 3]) + (255,))
            elif ct == 4:
                g, a = line[x * 2], line[x * 2 + 1]
                r.append((g, g, g, a))
            else:
                g = line[x]
                r.append((g, g, g, 255))
        img.append(r)
    return img
