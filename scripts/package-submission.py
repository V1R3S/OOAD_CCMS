"""Build a source-and-document submission ZIP without local tools or user data."""
from pathlib import Path
import hashlib
import zipfile

ROOT = Path(__file__).resolve().parents[1]
ROOT_FILES = (
    '.gitignore', '.gitattributes', 'README.md', 'FINAL-REVIEW.md',
    'presentation-guide.md', 'aau.png', 'pom.xml', 'build-app.ps1',
    'build-app.sh', 'start-app.ps1', 'start-app.cmd',
    'output/presentation/CCMS-OOAD-presentation.pptx',
)
TREES = ('src', 'docs', 'requirements', 'sample-data', 'prototype', 'scripts', 'output/pdf')
EXTENSIONS = {'.java', '.bin', '.md', '.tex', '.png', '.svg', '.csv', '.txt',
              '.pdf', '.py', '.ps1', '.html', '.css', '.js', '.cjs'}


def collect_files():
    files = [ROOT / name for name in ROOT_FILES]
    for tree in TREES:
        for path in sorted((ROOT / tree).rglob('*')):
            if '__pycache__' in path.parts or not path.is_file():
                continue
            if path.is_symlink() or not path.resolve().is_relative_to(ROOT):
                raise ValueError(f'Unexpected link outside the source tree: {path}')
            if path.suffix not in EXTENSIONS:
                raise ValueError(f'Review unexpected submission file: {path}')
            if path.suffix == '.bin' and path.relative_to(ROOT).as_posix() != 'src/test/resources/legacy-campus-v1.bin':
                raise ValueError(f'User data must not be submitted: {path}')
            files.append(path)
    for path in files:
        if not path.is_file():
            raise FileNotFoundError(path)
    return sorted(files)


def main():
    files = collect_files()
    destination = ROOT / 'dist' / 'CCMS-submission.zip'
    destination.parent.mkdir(exist_ok=True)
    manifest = []
    with zipfile.ZipFile(destination, 'w', compression=zipfile.ZIP_DEFLATED, compresslevel=9) as archive:
        for path in files:
            relative = path.relative_to(ROOT).as_posix()
            data = path.read_bytes()
            archive.writestr('CCMS/' + relative, data)
            manifest.append(f'{hashlib.sha256(data).hexdigest()}  {relative}')
        archive.writestr('CCMS/submission-manifest.sha256', '\n'.join(manifest) + '\n')
    with zipfile.ZipFile(destination) as archive:
        if archive.testzip() is not None:
            raise RuntimeError('ZIP integrity check failed.')
    checksum = hashlib.sha256(destination.read_bytes()).hexdigest()
    destination.with_suffix('.zip.sha256').write_text(checksum + '  ' + destination.name + '\n', encoding='ascii')
    print(f'{destination}: {len(files)} project files + manifest; {destination.stat().st_size:,} bytes')


if __name__ == '__main__':
    main()
