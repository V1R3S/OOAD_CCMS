"""Generate the source-checked Arena design class diagrams in the report UML style.

The overview includes every named production type. Focused sheets provide readable
detail. Anonymous Swing adapters and JDK classes are external implementation details.
Uses only Python's standard library; SVGs remain editable vector documents.
"""
from pathlib import Path
from html import escape
import re

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'reports/assets/uml'
INK, RED, GREEN, PINK, MUTED = '#243043', '#862C4C', '#EAF5F1', '#F9F1F4', '#5D6877'


class Diagram:
    def __init__(self, title, width=1800, height=1120):
        self.w, self.h, self.items, self.boxes = width, height, [], {}
        self.items.append(f'<svg xmlns="http://www.w3.org/2000/svg" width="{width}px" height="{height}px" viewBox="0 0 {width} {height}">')
        self.items.append(f'<defs><marker id="arrow" markerWidth="14" markerHeight="14" refX="12" refY="7" orient="auto"><path d="M2 2 L12 7 L2 12" fill="none" stroke="{INK}" stroke-width="1.5"/></marker><marker id="triangle" markerWidth="16" markerHeight="16" refX="14" refY="8" orient="auto"><path d="M2 2 L14 8 L2 14 Z" fill="white" stroke="{INK}" stroke-width="1.5"/></marker></defs>')
        self.items.append(f'<rect width="{width}" height="{height}" fill="white"/><rect width="{width}" height="11" fill="{RED}"/>')
        self.text(75,64,'ARENA / AL AIN UNIVERSITY / SYSTEM DESIGN',18,MUTED,True)
        self.text(75,130,title,42,INK,True)
        self.line([(75,164),(width-75,164)],color='#C4CDD7')
        self.line([(75,height-75),(width-75,height-75)],color='#C4CDD7')
        self.text(75,height-40,'AAU · OBJECT-ORIENTED ANALYSIS & DESIGN · UNITED ARAB EMIRATES',17,MUTED)

    def text(self,x,y,value,size=22,color=INK,bold=False,anchor='start'):
        self.items.append(f'<text x="{x}" y="{y}" font-family="DejaVu Sans, sans-serif" font-size="{size}" font-weight="{700 if bold else 400}" fill="{color}" text-anchor="{anchor}">{escape(value)}</text>')

    def line(self,points,kind=None,dashed=False,color=INK):
        marker=f' marker-end="url(#{kind})"' if kind else ''
        dash=' stroke-dasharray="10 7"' if dashed else ''
        self.items.append(f'<polyline points="{" ".join(str(x)+","+str(y) for x,y in points)}" fill="none" stroke="{color}" stroke-width="2"{dash}{marker}/>')

    def box(self,name,x,y,w,h,fields=(),methods=(),kind='class',tint=GREEN,size=21):
        self.boxes[name]=(x,y,w,h)
        self.items.append(f'<g data-class="{escape(name)}"><rect x="{x}" y="{y}" width="{w}" height="{h}" rx="9" fill="white" stroke="{INK}" stroke-width="2.2"/><rect x="{x+1}" y="{y+1}" width="{w-2}" height="72" rx="8" fill="{tint}"/>')
        self.text(x+w/2,y+26,'«'+kind+'»',16,MUTED,False,'middle')
        self.text(x+w/2,y+57,name,26,INK,True,'middle')
        self.line([(x,y+74),(x+w,y+74)])
        yy=y+105
        for value in fields:
            self.text(x+18,yy,value,size);yy+=29
        if methods:
            self.line([(x,yy-10),(x+w,yy-10)],color='#C4CDD7');yy+=20
            for value in methods:self.text(x+18,yy,value,size);yy+=29
        self.items.append('</g>')

    def save(self,name,notes=()):
        for i,note in enumerate(notes):self.text(75,self.h-135+i*26,note,18,MUTED)
        (OUT/name).write_text('\n'.join(self.items+['</svg>']),encoding='utf-8')


UI = {
    'ArenaApp': (['− service: ArenaService'], ['+ main(args): void', '~ showCompetition(cid): void']),
    'AppDialogs': (['− app: ArenaApp', '− service: ArenaService'], ['~ competitionDialog(c): void']),
    'CompetitionViews': (['− app: ArenaApp', '− service: ArenaService'], ['~ bracket(c), fixtures(c): JPanel']),
    'ArenaTheme': (['~ ACCENT, GREEN, PAPER: Color'], ['~ actionButton(text, primary): JButton']),
    'UiComponents': ([], ['~ grid(columns, rows): JTable', '~ paragraph(text, background): JTextArea']),
    'WorkspacePanel': ([], ['+ getScrollableTracksViewportWidth(): boolean']),
}
APP = {
    'ArenaService': (['− state: State', '− repository: Repository', '− session: String', '− clock: Clock'], ['+ createCompetition(...): Competition', '+ recordMatchScore(id, left, right): void', '+ standings(cid): List<Standing>']),
    'Repository': (['− file: Path', '− channel: FileChannel', '− lock: FileLock'], ['+ load(): State', '+ save(state): void', '+ close(): void']),
    'DemoData': ([], ['+ populate(state, now): void']),
    'Passwords': ([], ['+ hash(password): String', '+ verify(password, encoded): boolean']),
    'UserSummary': (['− id, name, email: String', '− role: Role'], []),
}
SCORING = {
    'CompetitionRules': ([], ['+ validate(a, b, c): void', '+ score(e): double', '+ requiresAllJudges(): boolean']),
    'ProjectRules': ([], ['+ score(e): double', '+ requiresAllJudges(): boolean']),
    'ProgrammingRules': ([], ['+ validate(a, b, c): void', '+ score(e): double']),
    'QuizRules': (['− maximum: int'], ['+ validate(a, b, c): void']),
    'KnockoutRules': ([], ['+ requiresSubmission(): boolean']),
}
TOURNAMENT = {
    'TournamentRules': ([], ['+ start(cid, teamIds): List<Match>', '+ nextRound(cid, matches): List<Match>', '+ matchesComplete(matches): boolean']),
    'ScoreRankingRules': ([], ['+ start(cid, teams): List<Match>']),
    'KnockoutTournamentRules': ([], ['+ nextRound(cid, matches): List<Match>']),
    'RoundRobinRules': ([], ['+ table(teams, matches, name):', '    List<LeagueStanding>']),
}
DOMAIN = {
    'Model': ([], ['+ legacyType(format): CompetitionType', '+ legacyTournament(format): TournamentFormat'], 'class'),
    'State': (['− schema: int', '− users, teams, competitions: Map', '− registrations, submissions: Map', '− evaluations, matches: Map', '− audit: List<Audit>'], ['+ upgradeSchema(): void'], 'class'),
    'User': (['− id, name, email: String', '− role: Role', '− password: String'], [], 'record'),
    'Team': (['− id, name, captainId: String', '− members: List<String>'], [], 'record'),
    'Competition': (['− id, title, description: String', '− type: CompetitionType', '− tournamentFormat: TournamentFormat', '− phase: Phase; format: Format (legacy)', '− capacity, teamSize, quizMaxPoints: int', '− organizerId: String; judgeIds: Set<String>', '− deadline: LocalDateTime'], ['+ withPhase(next): Competition'], 'record'),
    'Registration': (['− id, competitionId, teamId: String'], [], 'record'),
    'Submission': (['− id, competitionId, teamId: String', '− title, description, url: String', '− submittedAt: LocalDateTime'], [], 'record'),
    'Evaluation': (['− id, competitionId, teamId, judgeId: String', '− a, b, c: int; feedback: String'], [], 'record'),
    'Match': (['− id, competitionId: String; round: int', '− leftTeamId, rightTeamId, winnerId: String', '− leftScore, rightScore: Integer', '− drawn: boolean'], ['+ result(left, right): Match', '+ bye(), decided(): boolean'], 'record'),
    'Standing': (['− rank: int; teamId, team: String', '− score: double; detail: String'], [], 'record'),
    'LeagueStanding': (['− rank, played, won, drawn, lost: int', '− scored, conceded, points: int', '− teamId, team: String'], ['+ difference(): int'], 'record'),
    'Audit': (['− time: LocalDateTime', '− actor, action: String'], [], 'record'),
    'Role': (['ADMIN, ORGANIZER, JUDGE, STUDENT'], [], 'enumeration'),
    'Format': (['PROJECT, PROGRAMMING, KNOCKOUT', 'Compatibility field for version-one data'], [], 'enumeration'),
    'CompetitionType': (['PROJECT, PROGRAMMING, QUIZ, MATCH'], ['+ supports(format): boolean'], 'enumeration'),
    'TournamentFormat': (['SCORE_BASED, KNOCKOUT, ROUND_ROBIN'], [], 'enumeration'),
    'Phase': (['DRAFT, REGISTRATION, ACTIVE,', 'JUDGING, PUBLISHED, ARCHIVED'], [], 'enumeration'),
}


def verify_source():
    found=set()
    for path in (ROOT/'src/main/java').rglob('*.java'):
        found.update(re.findall(r'^\s*(?:(?:public|private|protected|static|final|abstract)\s+)*(?:class|interface|enum|record)\s+(\w+)',path.read_text(encoding='utf-8'),re.M))
    shown=set(UI)|set(APP)|set(SCORING)|set(TOURNAMENT)|set(DOMAIN)
    if found != shown:raise ValueError(f'Diagram class coverage differs: missing={found-shown}, extra={shown-found}')
    return found


def overview():
    d=Diagram('Design Class Diagram · Complete Production Type Overview',3600,3480)
    d.text(75,212,'PRESENTATION  ·  edu.aau.arena.ui',26,RED,True)
    for i,(name,(fields,methods)) in enumerate(UI.items()):
        d.box(name,60+i*580,245,550,220,fields,methods,size=18)
    # The boundary owns dialogs and views and delegates use cases to the service.
    d.line([(335,465),(335,505),(990,505),(990,465)],'arrow',True)
    d.line([(335,505),(1570,505),(1570,465)],'arrow',True)
    d.text(660,495,'creates / delegates',18,MUTED)
    d.text(75,565,'APPLICATION CONTROL, PERSISTENCE, AND SUPPORT',26,RED,True)
    for i,(name,(fields,methods)) in enumerate(APP.items()):
        d.box(name,60+i*700,600,660,345,fields,methods,'record' if name=='UserSummary' else 'class',PINK,size=21)
    d.line([(335,505),(30,505),(30,770),(60,770)],'arrow',True)
    d.line([(720,770),(760,770)],'arrow');d.text(724,750,'1',16)
    d.line([(390,945),(390,980),(2470,980),(2470,945)],'arrow',True)
    d.text(75,1050,'SCORING STRATEGIES',26,RED,True)
    d.text(1850,1050,'TOURNAMENT STRATEGIES',26,RED,True)
    d.box('CompetitionRules',60,1080,1620,215,*SCORING['CompetitionRules'],'interface',GREEN,size=23)
    d.box('TournamentRules',1850,1080,1620,215,*TOURNAMENT['TournamentRules'],'interface',GREEN,size=23)
    d.line([(390,980),(390,1080)],'arrow',True)
    d.line([(390,980),(2660,980),(2660,1080)],'arrow',True)
    for i,name in enumerate(list(SCORING)[1:]):
        x=60+i*415;d.box(name,x,1380,385,220,*SCORING[name],size=16)
        d.line([(x+192,1380),(x+192,1335),(870,1335),(870,1295)],'triangle',True)
    for i,name in enumerate(list(TOURNAMENT)[1:]):
        x=1850+i*550;d.box(name,x,1380,520,220,*TOURNAMENT[name],size=19)
        d.line([(x+260,1380),(x+260,1335),(2660,1335),(2660,1295)],'triangle',True)
    d.text(75,1680,'PERSISTED MODEL AND RESULT RECORDS  ·  edu.aau.ccms.Model (nested types; names retained for compatibility)',26,RED,True)
    for i,(name,(fields,methods,kind)) in enumerate(DOMAIN.items()):
        x=60+(i%6)*580;y=1720+(i//6)*480
        d.box(name,x,y,550,440,fields,methods,kind,GREEN if kind=='enumeration' else PINK,size=19)
    d.save('08_design_class.svg',[
        'All 37 named production types are shown. Compartments show selected real fields and operations; record fields are private with public generated accessors.',
        'Solid arrow: navigable association. Dashed open arrow: dependency. Dashed hollow triangle: interface realization. See the focused sheets for readable detail and relationships.'
    ])


def application_detail():
    d=Diagram('Design Class Diagram · Application and UI',2400,1660)
    for i,(name,(fields,methods)) in enumerate(UI.items()):
        x=60+(i%3)*780;y=220+(i//3)*275
        d.box(name,x,y,735,225,fields,methods,size=22)
    d.box('ArenaService',60,860,750,420,*APP['ArenaService'],tint=PINK,size=24)
    d.box('Repository',920,860,650,420,*APP['Repository'],size=24)
    d.box('UserSummary',1680,860,650,230,*APP['UserSummary'],'record',size=24)
    d.box('DemoData',1680,1120,650,165,*APP['DemoData'],size=24)
    d.box('Passwords',60,1350,750,180,*APP['Passwords'],size=24)
    d.line([(427,445),(427,470),(1207,470),(1207,445)],'arrow',True)
    d.line([(427,470),(1987,470),(1987,445)],'arrow',True)
    d.line([(60,335),(25,335),(25,1000),(60,1000)],'arrow',True)
    d.line([(810,1010),(920,1010)],'arrow');d.text(860,990,'1',20)
    d.line([(435,1280),(435,1350)],'arrow',True)
    d.line([(810,1210),(850,1210),(850,1310),(2005,1310),(2005,1285)],'arrow',True)
    d.line([(810,900),(855,900),(855,810),(2005,810),(2005,860)],'arrow',True)
    d.text(945,1375,'ArenaApp extends JFrame.',24)
    d.text(945,1410,'WorkspacePanel extends JPanel and implements Scrollable.',24)
    d.text(945,1445,'Repository implements AutoCloseable.',24)
    d.save('08a_application_classes.svg')


def domain_detail():
    d=Diagram('Design Class Diagram · Records and Relationships',2400,1940)
    layout={'User':(70,240,510,235),'Team':(800,240,600,235),'Competition':(1650,240,680,440),
            'Registration':(800,750,600,175),'Submission':(70,750,610,235),
            'Evaluation':(800,1110,690,220),'Match':(1650,820,680,365),
            'State':(70,1370,610,350),'Audit':(800,1500,690,220),
            'Standing':(1650,1240,680,190),'LeagueStanding':(1650,1480,680,260)}
    for name,(x,y,w,h) in layout.items():
        f,m,k=DOMAIN[name];d.box(name,x,y,w,h,f,m,k,size=21)
    d.line([(580,355),(800,355)]);d.text(605,333,'1..8',20);d.text(740,333,'0..*',20);d.text(615,389,'members',20,RED)
    d.line([(310,240),(310,202),(1990,202),(1990,240)]);d.text(780,195,'owner: 1 User / 0..* Competitions; judges: 1..* Users / 0..* Competitions',20,RED)
    d.line([(1100,475),(1100,750)]);d.text(1120,515,'1 Team',20);d.text(1120,724,'0..* registrations',20)
    d.line([(1650,600),(1510,600),(1510,830),(1400,830)]);d.text(1518,575,'1 Competition',20);d.text(1415,855,'0..*',20)
    d.line([(680,830),(800,830)]);d.text(686,810,'0..1',20);d.text(766,810,'1',20)
    d.line([(1145,926),(1145,1110)]);d.text(1160,960,'1 Registration',20);d.text(1160,1080,'0..* evaluations',20)
    d.line([(1990,680),(1990,820)]);d.text(2010,717,'1 Competition',20);d.text(2010,790,'0..* matches',20)
    d.line([(1400,400),(1570,400),(1570,945),(1650,945)]);d.text(1405,430,'1..2 teams / match',18,RED)
    d.text(1450,465,'1 when it is a bye',18,MUTED)
    d.line([(70,355),(25,355),(25,1260),(800,1260)]);d.text(40,1234,'judge: 1 User / 0..* Evaluations',20,RED)
    d.text(735,1380,'State owns the stored maps and audit list.',22,RED)
    d.text(735,1415,'Records link by IDs; no recursive object ownership.',22,MUTED)
    d.save('08b_record_classes.svg',[
        'Record components are private final fields with public accessors. Selected attributes are shown. Dashed dependencies and enumeration types appear on the overview.',
        'A registration may have no submission (quiz or match event). Evaluations belong to entries; only project/programming score events require a submission.'
    ])


def strategies():
    d=Diagram('Class Diagram · Scoring and Tournament Strategies',2400,1500)
    d.box('CompetitionRules',60,240,1080,335,
          [],['+ explanation(): String', '+ validate(a, b, c): void', '+ score(e): double; detail(results): String',
              '+ requiresSubmission(): boolean', '+ requiresAllJudges(): boolean', '+ forCompetition(c): CompetitionRules'],'interface',size=24)
    d.box('TournamentRules',1260,240,1080,335,[],['+ explanation(): String', '+ start(cid, teamIds): List<Match>',
        '+ nextRound(cid, matches): List<Match>', '+ matchesComplete(matches): boolean', '+ standings(teams, matches, name): List<Standing>',
        '+ forFormat(format): TournamentRules'],'interface',size=24)
    for i,name in enumerate(list(SCORING)[1:]):
        x=60+(i%2)*560;y=730+(i//2)*280
        d.box(name,x,y,520,220,*SCORING[name],size=21)
        # Shared interface realization tree, routed through the gap between rows.
        sx=x+260;route=650 if i<2 else 990
        if i>=2:d.line([(sx,y),(sx,route),(35,route),(35,650),(600,650),(600,575)],'triangle',True)
        else:d.line([(sx,y),(sx,650),(600,650),(600,575)],'triangle',True)
    for i,name in enumerate(list(TOURNAMENT)[1:]):
        x=1260+(i%2)*560;y=730+(i//2)*280
        d.box(name,x,y,520,220,*TOURNAMENT[name],size=19)
        sx=x+260
        if i==2:d.line([(sx,y),(sx,990),(1205,990),(1205,650),(1800,650),(1800,575)],'triangle',True)
        else:d.line([(sx,y),(sx,650),(1800,650),(1800,575)],'triangle',True)
    d.text(1840,1100,'ArenaService selects both strategies.',22,RED,True)
    d.text(1840,1140,'CompetitionType controls scoring.',21)
    d.text(1840,1175,'TournamentFormat controls fixtures.',21)
    d.save('07_strategy_class.svg', ['Solid code policies use two independent interfaces. KnockoutRules is the legacy-named no-submission/no-rubric policy for general matches.'])


if __name__=='__main__':
    OUT.mkdir(parents=True,exist_ok=True)
    types=verify_source()
    overview();application_detail();domain_detail();strategies()
    print(f'Generated four source-checked UML SVGs covering {len(types)} named production types.')
