"""Generate editable SVG and matching PNG wireframes (Pillow required)."""
from pathlib import Path
from html import escape
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'reports/assets/wireframes'
OUT.mkdir(parents=True, exist_ok=True)
FONT = Path('C:/Windows/Fonts/segoeui.ttf')
FALLBACK = '/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf'

def render(name, heading, subtitle, kind):
    image = Image.new('RGB', (1440, 960), '#fafafa')
    draw = ImageDraw.Draw(image)
    svg = ['<svg xmlns="http://www.w3.org/2000/svg" width="1440" height="960" viewBox="0 0 1440 960">', '<rect width="1440" height="960" fill="#fafafa"/>']
    def rect(x,y,w,h,fill='#ffffff',stroke='#a8afb0'):
        draw.rectangle((x,y,x+w,y+h),fill=fill,outline=stroke,width=2)
        svg.append(f'<rect x="{x}" y="{y}" width="{w}" height="{h}" fill="{fill}" stroke="{stroke}" stroke-width="2"/>')
    def text(x,y,value,size=22,color='#394448'):
        font=ImageFont.truetype(str(FONT if FONT.exists() else FALLBACK),size)
        draw.text((x,y),value,font=font,fill=color)
        svg.append(f'<text x="{x}" y="{y+size}" fill="{color}" font-size="{size}" font-family="Segoe UI, sans-serif">{escape(value)}</text>')
    def line(x,y,w):
        draw.line((x,y,x+w,y),fill='#d2d7d8',width=6)
        svg.append(f'<path d="M{x} {y} h{w}" stroke="#d2d7d8" stroke-width="6"/>')
    rect(0,0,230,960,'#ecefef')
    text(28,34,'A / ARENA',27)
    text(28,82,'INTERFACE SKELETON',13)
    for i,label in enumerate(['Overview','Competitions','My teams','Judging studio','Results','People','Activity']):
        if i == (0 if kind=='overview' else 1): rect(18,156+i*63,194,46,'#d6dcdc')
        text(33,165+i*63,label,20)
    text(28,860,'Role / display name',16)
    text(28,902,'Sign out',18)
    text(274,34,'WIREFRAME  /  STRUCTURE BEFORE STYLING',14)
    text(274,76,heading,34)
    text(274,129,subtitle,19)
    if kind=='overview':
        rect(274,190,1100,215,'#edf0f0');text(302,214,'Featured challenge / welcome message',28)
        line(302,276,640);line(302,310,520)
        rect(302,340,235,44,'#d4dbdb');text(323,350,'Explore competitions',18)
        for i,(count,label) in enumerate([('05','Competitions'),('01','Open registration'),('05','Campus teams')]):
            rect(274+i*374,432,350,115);text(294+i*374,449,label,18);text(294+i*374,485,count,36)
        text(274,578,'Find your next challenge',25)
        for i,kind_name in enumerate(['Project showcase','Programming contest','Knockout bracket']):
            x=274+i*374;rect(x,626,350,245);rect(x,626,350,65,'#edf0f0')
            text(x+20,645,kind_name,19);text(x+20,711,'Competition title',22)
            line(x+20,758,295);text(x+20,781,'Status / team count',16)
            text(x+20,827,'Explore competition',18)
    elif kind=='competition':
        rect(274,190,1100,208);text(300,213,'Type, tournament format, rules, team limit, deadline',24)
        line(300,273,960)
        for i,label in enumerate(['Draft','Registration','Active','Judging','Published','Archived']):
            rect(300+i*175,322,160,45,'#dde2e2' if i==1 else '#f2f4f4');text(310+i*175,333,label,17)
        rect(274,423,260,52,'#d4dbdb');text(296,437,'Primary role action',20)
        text(274,516,'Teams  |  Submissions  |  Reviews  |  Leaderboard',21)
        rect(274,563,1100,290);text(300,582,'Team name                      Captain                          Members',21)
        for y in [638,698,758,818]:line(300,y,1040)
    elif kind=='judging':
        rect(274,190,1100,92,'#edf0f0');text(300,211,'Assigned competition / Judging phase',23);text(300,247,'Project rubric: innovation 40%, execution 35%, presentation 25%',18)
        rect(274,313,510,482);text(298,338,'Submission preview',25)
        for y in [412,457,502,547]:line(298,y,454)
        text(298,604,'Repository / project link',18);text(298,690,'Read before scoring',18)
        rect(814,313,560,482);text(842,338,'Evaluate a team',25)
        for i,label in enumerate(['Team selector','Innovation / 100','Execution / 100','Presentation / 100']):
            text(842,392+i*62,label,19);rect(1120,384+i*62,224,44)
        text(842,646,'Written feedback',19);rect(842,683,502,80)
        rect(1130,823,244,54,'#d4dbdb');text(1150,836,'Save evaluation',20)
    else:
        text(274,194,'Registration order sets the seed. Each match needs one winner.',20)
        for x,label in [(274,'ROUND 1'),(690,'FINAL'),(1100,'CHAMPION')]:text(x,258,label,18)
        for y,a,b in [(322,'Team Alpha','Code Warriors'),(590,'Innovators','Circuit Breakers')]:
            rect(274,y,330,180);text(300,y+25,a,23);text(300,y+75,b,23);text(300,y+132,'Record winner',18)
        rect(690,456,330,180);text(716,485,'Winner of match 1',22);text(716,540,'Winner of match 2',22);text(716,593,'Record final winner',18)
        rect(1100,471,274,150,'#edf0f0');text(1120,495,'Campus champion',22);text(1120,552,'Publish results',18)
        line(605,412,42);line(605,680,42);line(1020,548,78)
    text(274,921,'Low-fidelity planning artifact. Final screens are documented separately.',16)
    svg.append('</svg>')
    image.save(OUT / f'{name}.png')
    (OUT / f'{name}.svg').write_text('\n'.join(svg),encoding='utf-8')

render('01-overview','Overview','Information hierarchy, navigation, and competition entry points.','overview')
render('02-competition','Competition workspace','Rules and lifecycle stay visible above the team and work tabs.','competition')
render('03-judging','Judging workspace','Review the submission, apply the rubric, and save clear feedback.','judging')
render('04-bracket','Knockout bracket','Progression from seeded matches to an official champion.','bracket')
print('Generated four editable SVG wireframes and four PNG previews.')
