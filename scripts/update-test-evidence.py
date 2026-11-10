"""Copy the latest executed test evidence and produce the LaTeX results table."""
from pathlib import Path
import csv
import shutil

root = Path(__file__).resolve().parents[1]
evidence = root / 'reports/test-evidence'
evidence.mkdir(parents=True, exist_ok=True)
for source, destination in [('test-results.csv', 'service-test-results.csv'),
                            ('test-summary.txt', 'service-test-summary.txt'),
                            ('ui-test-results.csv', 'ui-test-results.csv'),
                            ('ui-test-summary.txt', 'ui-test-summary.txt'),
                            ('arena-test-results.csv', 'arena-test-results.csv'),
                            ('arena-test-summary.txt', 'arena-test-summary.txt'),
                            ('prototype-test-summary.txt', 'prototype-test-summary.txt')]:
    shutil.copy2(root / 'build' / source, evidence / destination)
rows = list(csv.DictReader((evidence / 'service-test-results.csv').open(encoding='utf-8')))
def escape(value):
    return value.replace('&',r'\&').replace('_',r'\_').replace('%',r'\%')
text = r'\small\begin{longtable}{p{0.12\linewidth}p{0.66\linewidth}p{0.1\linewidth}}\toprule ID & Requirement / executed check & Result\\\midrule\endhead' + '\n'
text += '\n'.join(escape(r['Test ID'])+' & '+escape(r['Description'])+' & '+r['Status']+r'\\' for r in rows)
text += '\n'+r'\bottomrule\end{longtable}\normalsize'+'\n'
(evidence / 'service-results-table.tex').write_text(text,encoding='utf-8')
print(f'Preserved {len(rows)} executed test results.')
ui_rows = list(csv.DictReader((evidence / 'ui-test-results.csv').open(encoding='utf-8')))
ui_table = r'\begingroup\renewcommand{\arraystretch}{1.05}\footnotesize\begin{longtable}{p{0.12\linewidth}p{0.66\linewidth}p{0.1\linewidth}}\toprule ID & Swing interaction check & Result\\\midrule\endhead' + '\n'
ui_table += '\n'.join(escape(row['Test ID'])+' & '+escape(row['Description'])+' & '+row['Status']+r'\\' for row in ui_rows)
ui_table += '\n'+r'\bottomrule\end{longtable}\endgroup'+'\n'
(evidence / 'swing-results-table.tex').write_text(ui_table, encoding='utf-8')
print(f'Preserved {len(ui_rows)} executed Swing UI results.')
arena_rows = list(csv.DictReader((evidence / 'arena-test-results.csv').open(encoding='utf-8')))
arena_table = r'\small\begin{longtable}{p{0.12\linewidth}p{0.66\linewidth}p{0.1\linewidth}}\toprule ID & Arena regression check & Result\\\midrule\endhead' + '\n'
arena_table += '\n'.join(escape(row['Test ID'])+' & '+escape(row['Description'])+' & '+row['Status']+r'\\' for row in arena_rows)
arena_table += '\n'+r'\bottomrule\end{longtable}\normalsize'+'\n'
(evidence / 'arena-results-table.tex').write_text(arena_table, encoding='utf-8')
print(f'Preserved {len(arena_rows)} Arena regression results.')
