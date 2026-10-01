/**
 * Lightweight, zero-dependency QR Code generator (Version 2, 25x25 matrix, Byte Mode)
 * Capable of encoding standard strings like "HAPPYPAWS:PET:1:Bella:HP-LR-2024-0884"
 */

// Error correction and Galois Field log/exp tables for QR code generation
const EXP_TABLE = new Uint8Array(256);
const LOG_TABLE = new Uint8Array(256);

(function initGaloisField() {
  let x = 1;
  for (let i = 0; i < 255; i++) {
    EXP_TABLE[i] = x;
    LOG_TABLE[x] = i;
    x <<= 1;
    if (x & 0x100) x ^= 0x11d;
  }
  EXP_TABLE[255] = EXP_TABLE[0];
})();

function gfMultiply(x: number, y: number): number {
  if (x === 0 || y === 0) return 0;
  return EXP_TABLE[(LOG_TABLE[x] + LOG_TABLE[y]) % 255];
}

function polyMultiply(p1: number[], p2: number[]): number[] {
  const result = new Array(p1.length + p2.length - 1).fill(0);
  for (let i = 0; i < p1.length; i++) {
    for (let j = 0; j < p2.length; j++) {
      result[i + j] ^= gfMultiply(p1[i], p2[j]);
    }
  }
  return result;
}

function getGeneratorPoly(degree: number): number[] {
  let poly = [1];
  for (let i = 0; i < degree; i++) {
    poly = polyMultiply(poly, [1, EXP_TABLE[i]]);
  }
  return poly;
}

function calculateEccBytes(data: number[], eccLength: number): number[] {
  const gen = getGeneratorPoly(eccLength);
  const remainder = data.concat(new Array(eccLength).fill(0));
  for (let i = 0; i < data.length; i++) {
    const factor = remainder[i];
    if (factor !== 0) {
      for (let j = 0; j < gen.length; j++) {
        remainder[i + j] ^= gfMultiply(gen[j], factor);
      }
    }
  }
  return remainder.slice(data.length);
}

export function generateQrMatrix(text: string): boolean[][] {
  const size = 29; // Version 3 QR code (29x29) handles up to 70 chars comfortably
  const matrix: boolean[][] = Array.from({ length: size }, () => Array(size).fill(false));
  const isFunction: boolean[][] = Array.from({ length: size }, () => Array(size).fill(false));

  function setFinder(startX: number, startY: number) {
    for (let dy = -1; dy <= 7; dy++) {
      for (let dx = -1; dx <= 7; dx++) {
        const x = startX + dx;
        const y = startY + dy;
        if (x >= 0 && x < size && y >= 0 && y < size) {
          isFunction[y][x] = true;
          const isBorder = dx === -1 || dx === 7 || dy === -1 || dy === 7;
          const isOuter = dx === 0 || dx === 6 || dy === 0 || dy === 6;
          const isInner = dx >= 2 && dx <= 4 && dy >= 2 && dy <= 4;
          matrix[y][x] = !isBorder && (isOuter || isInner);
        }
      }
    }
  }

  // 3 Finder patterns
  setFinder(0, 0);
  setFinder(size - 7, 0);
  setFinder(0, size - 7);

  // Alignment pattern at (size-9, size-9)
  for (let dy = -2; dy <= 2; dy++) {
    for (let dx = -2; dx <= 2; dx++) {
      const x = 22 + dx;
      const y = 22 + dy;
      if (x < size && y < size) {
        isFunction[y][x] = true;
        matrix[y][x] = Math.abs(dx) === 2 || Math.abs(dy) === 2 || (dx === 0 && dy === 0);
      }
    }
  }

  // Timing patterns
  for (let i = 8; i < size - 8; i++) {
    isFunction[6][i] = true;
    matrix[6][i] = i % 2 === 0;
    isFunction[i][6] = true;
    matrix[i][6] = i % 2 === 0;
  }

  // Dark module
  isFunction[size - 8][8] = true;
  matrix[size - 8][8] = true;

  // Format reserve
  for (let i = 0; i < 9; i++) {
    isFunction[8][i] = true;
    isFunction[i][8] = true;
  }
  for (let i = size - 8; i < size; i++) {
    isFunction[8][i] = true;
    isFunction[i][8] = true;
  }

  // Encode text bytes into bitstream
  const bytes = new TextEncoder().encode(text);
  const dataBits: number[] = [];
  function pushBits(val: number, len: number) {
    for (let i = len - 1; i >= 0; i--) {
      dataBits.push((val >> i) & 1);
    }
  }

  // Mode: Byte (0100)
  pushBits(4, 4);
  pushBits(bytes.length, 8);
  for (const b of bytes) {
    pushBits(b, 8);
  }

  // Terminator (up to 4 zeroes)
  const capacityBits = 44 * 8; // 44 bytes data capacity for Ver 3 M
  const padZeros = Math.min(4, capacityBits - dataBits.length);
  for (let i = 0; i < padZeros; i++) dataBits.push(0);

  // Align to byte
  while (dataBits.length % 8 !== 0) dataBits.push(0);

  // Pad bytes
  const dataBytes: number[] = [];
  for (let i = 0; i < dataBits.length; i += 8) {
    let byteVal = 0;
    for (let j = 0; j < 8; j++) byteVal = (byteVal << 1) | dataBits[i + j];
    dataBytes.push(byteVal);
  }

  let padToggle = 0xec;
  while (dataBytes.length < 44) {
    dataBytes.push(padToggle);
    padToggle = padToggle === 0xec ? 0x11 : 0xec;
  }

  const eccBytes = calculateEccBytes(dataBytes, 26);
  const allCodewords = dataBytes.concat(eccBytes);

  // Flatten all codewords into bits
  const finalBits: number[] = [];
  for (const b of allCodewords) {
    for (let i = 7; i >= 0; i--) {
      finalBits.push((b >> i) & 1);
    }
  }

  // Place bits into matrix
  let bitIndex = 0;
  let dir = -1; // upward
  for (let x = size - 1; x > 0; x -= 2) {
    if (x === 6) x--; // skip timing col
    const yStart = dir === -1 ? size - 1 : 0;
    const yEnd = dir === -1 ? -1 : size;
    for (let y = yStart; y !== yEnd; y += dir === -1 ? -1 : 1) {
      for (let c = 0; c < 2; c++) {
        const col = x - c;
        if (!isFunction[y][col]) {
          const bit = bitIndex < finalBits.length ? finalBits[bitIndex++] : 0;
          // Apply standard mask pattern 000: (row + col) % 2 === 0
          const mask = (y + col) % 2 === 0;
          matrix[y][col] = (bit === 1) !== mask;
        }
      }
    }
    dir = -dir;
  }

  // Format info (Mask 0, ECC M: 101010000010010)
  const formatStr = "101010000010010";
  let fIdx = 0;
  for (let i = 0; i <= 8; i++) {
    if (i !== 6) matrix[8][i] = formatStr[fIdx++] === '1';
  }
  for (let i = 7; i >= 0; i--) {
    if (i !== 6) matrix[i][8] = formatStr[fIdx++] === '1';
  }

  return matrix;
}
