/* Browser-only design prototype. The Java application remains the implementation. */
const $ = (selector) => document.querySelector(selector);
const escapeHtml = (value) => String(value).replace(/[&<>"']/g, c => ({'&':'&amp;','<':'&lt;','>':'&gt;','"':'&quot;',"'":'&#39;'}[c]));
const initialState = () => ({
  role: 'Student', team: 'Team Alpha', registered: false, submission: null, projectScores: null,
  programming: {solved: 5, minutes: 92, wrong: 2}, winners: [null, null, null],
  events: [
    {id:'innovation', title:'Campus Innovation Challenge', format:'Project showcase', type:'project', symbol:'✳', stage:'Registration', venue:'Innovation Lab', date:'12 November', teams:1, description:'Small ideas. Meaningful change. Build a project that makes campus life better.', tags:['2–4 students','Weighted rubric','Project submission']},
    {id:'code', title:'Code Sprint 2026', format:'Programming contest', type:'code', symbol:'{ }', stage:'Judging', venue:'Computing Lab 2', date:'18 November', teams:4, description:'Think clearly, code quickly. Solve a set of algorithmic challenges with your team.', tags:['2–3 students','10 problems','Penalty ranking']},
    {id:'robotics', title:'Robotics Knockout Cup', format:'Knockout bracket', type:'bracket', symbol:'⋈', stage:'Active', venue:'Engineering Arena', date:'24 November', teams:4, description:'Four teams. Two rounds. One champion. Follow every match on the way to the final.', tags:['2–4 students','Single elimination','4 seeded teams']}
  ]
});
let state = initialState();
let search = '', format = 'all', toastTimer;
const eventById = id => state.events.find(event => event.id === id);
const stageBadge = event => `<span class="status ${event.stage.toLowerCase()}">${event.stage === 'Registration' ? 'Registration open' : event.stage}</span>`;
const button = (text, action, extra = '', secondary = false) => `<button class="button${secondary ? ' secondary' : ''}" data-action="${action}" ${extra}>${text}</button>`;
function toast(message) { $('#toast').textContent = message; $('#toast').classList.add('visible'); clearTimeout(toastTimer); toastTimer = setTimeout(() => $('#toast').classList.remove('visible'), 4200); }
function heading(eyebrow, title, text, action = '') { return `<div class="page-heading"><div><div class="eyebrow">${eyebrow}</div><h1>${title}</h1><p>${text}</p></div>${action}</div>`; }
function card(event) { return `<article class="competition-card"><div class="card-art ${event.type}"><div class="eyebrow">${event.format}</div><span class="format-symbol" aria-hidden="true">${event.symbol}</span></div><div class="card-body">${stageBadge(event)}<h3>${event.title}</h3><p>${event.description}</p><div class="card-meta"><span>${event.date} · 2026</span><span>${event.teams + (event.id === 'innovation' && state.registered ? 1 : 0)} ${event.teams === 1 && !state.registered ? 'team' : 'teams'}</span></div><a class="card-link" href="#event/${event.id}">Explore competition <span aria-hidden="true">↗</span></a></div></article>`; }
function overview() {
  const name = {Student:'Khaled', Organizer:'Organizer', Judge:'Judge'}[state.role];
  return heading('YOUR CAMPUS, IN MOTION', `Good morning, ${name}.`, 'A place to challenge yourself. A community to do it with.', '<a class="button secondary" href="#competitions">Browse competitions <span>↗</span></a>') +
    `<section class="hero"><div class="hero-copy"><div class="eyebrow">THE NEXT GREAT IDEA STARTS HERE</div><h2>Bring your ideas.<br>Find your moment.</h2><p>From your first prototype to the final round, there’s a challenge with your name on it.</p><a class="button" href="#event/innovation">Explore the Innovation Challenge <span>→</span></a></div><div class="hero-art" aria-hidden="true"><div class="orbit"></div><div class="orbit second"></div><div class="orbit third"></div><div class="trophy">✳</div><span class="art-tag one">01 / IDEATE</span><span class="art-tag two">02 / BUILD & COMPETE</span><span class="art-dot"></span></div></section>
    <div class="stats"><div class="stat"><div class="stat-top">Competitions this term <span>◇</span></div><strong>03</strong><small>Three ways to compete</small></div><div class="stat"><div class="stat-top">My registrations <span>↗</span></div><strong>${state.registered ? '01' : '00'}</strong><small>${state.registered ? 'Innovation Challenge' : 'Your next challenge awaits'}</small></div><div class="stat"><div class="stat-top">My team <span>◎</span></div><strong>02 <span style="font-size:13px;letter-spacing:0">members</span></strong><small>${escapeHtml(state.team)} · Ready to collaborate</small></div></div>
    <div class="section-heading"><h2>Find your next challenge</h2><a href="#competitions">View all competitions →</a></div><div class="cards">${state.events.map(card).join('')}</div>
    <div class="bottom-note"><span aria-hidden="true">◷</span><p><strong>One platform. Different paths to winning.</strong><br>Project rubrics, programming scores, and knockout matches each have their own workflow.</p></div>`;
}
function competitions() {
  return heading('FIND YOUR CHALLENGE', 'Something for every ambition.', 'Explore the formats. Bring a team. Make your mark.') + `<div class="filters"><label class="search"><span class="eyebrow" style="display:none">Search competitions</span><input id="search" type="search" aria-label="Search competitions" placeholder="Search competitions…" value="${escapeHtml(search)}"></label><select id="format" aria-label="Filter by competition format"><option value="all">All formats</option><option value="project">Project showcase</option><option value="code">Programming contest</option><option value="bracket">Knockout bracket</option></select></div><div class="cards" id="competition-list">${filteredCards()}</div>`;
}
function filteredCards() { const events = state.events.filter(e => (format === 'all' || format === e.type) && `${e.title} ${e.format}`.toLowerCase().includes(search.toLowerCase())); return events.length ? events.map(card).join('') : '<div class="empty">No competitions match your search. Try another name or format.</div>'; }
function phases(event) { const phases = ['Draft','Registration','Active','Judging','Published','Archived']; return `<div class="phase-strip" aria-label="Competition lifecycle">${phases.map((p,i) => `<div class="phase ${p === event.stage ? 'current' : i < phases.indexOf(event.stage) ? 'done' : ''}">${p}</div>`).join('')}</div>`; }
function eventDetail(id) {
  const event = eventById(id); if (!event) return '<div class="empty">Competition not found. <a href="#competitions">Browse competitions</a></div>';
  const rules = {
    project:'<li>Innovation: 40% of the final score.</li><li>Execution: 35% of the final score.</li><li>Presentation: 25% of the final score.</li><li>Every assigned judge reviews every submitted project.</li>',
    code:'<li>Teams are ranked by problems solved, highest first.</li><li>Ties use elapsed time + 20 minutes per wrong attempt.</li><li>An assigned judge enters the official result.</li><li>This system records results; it does not execute code.</li>',
    bracket:'<li>Four teams enter a single-elimination bracket.</li><li>Teams are seeded in registration order.</li><li>An assigned judge confirms each match winner.</li><li>Winners advance to the final; a champion is required to publish.</li>'
  };
  return `<a class="back" href="#competitions">← All competitions</a>${heading(event.format, event.title, event.description, stageBadge(event))}${phases(event)}<div class="detail-grid"><div><section class="panel"><h2>The challenge</h2><p>${event.type === 'project' ? 'Design and demonstrate a practical solution to a campus problem. Your team will submit a project link and a short description before the judging round.' : event.type === 'code' ? 'Work through ten problems with increasing difficulty. The official score combines how many problems your team solves and the time penalties it receives.' : 'Follow the tournament from the opening matches to the championship. A confirmed winner moves directly into the next round.'}</p><div class="pill-row">${event.tags.map(t => `<span class="pill">${t}</span>`).join('')}</div></section><section class="panel"><h2>How winning works</h2><ul>${rules[event.type]}</ul></section>${event.type === 'bracket' ? bracketPanel(event) : event.type === 'code' ? programmingTable(event) : projectSubmission()}</div><aside><section class="panel"><h2>At a glance</h2><div class="key-value"><span>Date</span><strong>${event.date} 2026</strong></div><div class="key-value"><span>Location</span><strong>${event.venue}</strong></div><div class="key-value"><span>Format</span><strong>${event.format}</strong></div><div class="key-value"><span>Stage</span><strong>${event.stage}</strong></div></section><section class="panel"><h2>${state.role === 'Organizer' ? 'Manage competition' : state.role === 'Judge' ? 'Your judging desk' : 'Your next step'}</h2>${eventActions(event)}</section></aside></div>`;
}
function eventActions(event) {
  if (event.stage === 'Published') return '<p>Results have been certified and are ready to view.</p><a class="button full" href="#results">View results →</a>';
  if (state.role === 'Organizer') {
    const next = {Registration:'Active',Active:'Judging',Judging:'Published'}[event.stage];
    return `<p>${event.stage === 'Registration' ? 'Close registration to start the competition.' : event.stage === 'Active' ? (event.type === 'bracket' ? 'Complete all matches, then certify the bracket before publication.' : 'Collect the project submission before opening judging.') : 'Complete all evaluations before publishing the results.'}</p>${button(next === 'Published' ? 'Publish results' : `Move to ${next}`, 'advance', `data-id="${event.id}"`)}<div class="callout">Prototype walkthrough: switch roles in the top bar to complete each step.</div>`;
  }
  if (state.role === 'Judge') return (event.type === 'bracket' ? event.stage === 'Active' : event.stage === 'Judging') ? (event.type === 'bracket' ? '<p>Select the winner of each match in the bracket. Winners advance automatically.</p>' : `<p>${event.type === 'project' ? 'Review Team Alpha’s submission against the weighted rubric.' : 'Record the official programming result for Team Alpha.'}</p>${button(event.type === 'project' ? 'Review project' : 'Enter official score', event.type === 'project' ? 'review-project' : 'review-code')}`) : '<p>Scoring opens during Judging. Knockout match decisions are recorded during Active.</p>';
  if (event.type !== 'project') return '<p>Registration has closed for this competition. Follow the current round here.</p><div class="callout">To try scoring or match progression, choose Judge in the top bar.</div>';
  if (!state.registered) return event.stage === 'Registration' ? `<p>Enter with ${escapeHtml(state.team)}. Your two-person roster will be locked for this competition.</p>${button('Register my team', 'register')}` : '<p>Registration is closed for this competition.</p>';
  return `<span class="status">Your team is registered</span><p>${event.stage === 'Registration' ? 'Your place is confirmed. Switch to Organizer and move the event to Active to try a submission.' : event.stage === 'Active' ? 'Submit a project link and description for the judges.' : 'Judging is in progress. Results become visible after publication.'}</p>${event.stage === 'Active' ? button(state.submission ? 'Edit submission' : 'Submit project', 'submit-project') : ''}`;
}
function projectSubmission() { return `<section class="panel"><h2>Project submissions</h2><div class="key-value"><span>Team Beta</span><strong>Smart Campus Garden</strong></div>${state.submission ? `<div class="key-value"><span>${escapeHtml(state.team)}</span><strong>${escapeHtml(state.submission.title)}</strong></div><p style="margin-top:16px">${escapeHtml(state.submission.description)}</p><p><a href="${escapeHtml(state.submission.url)}" target="_blank" rel="noopener noreferrer">Open submitted project ↗</a></p>` : '<p style="margin-top:16px">Your project will appear here after submission.</p>'}</section>`; }
function programmingTable(event) {
  if (event.stage !== 'Published' && state.role === 'Student') return '<section class="panel"><h2>Standings</h2><p>Official scores are visible to participants after publication.</p></section>';
  const p = state.programming; const rows = [{name:'Team Alpha', solved:p.solved, penalty:p.minutes + 20*p.wrong},{name:'Team Beta',solved:5,penalty:145},{name:'Team Gamma',solved:4,penalty:117},{name:'Team Delta',solved:3,penalty:128}].sort((a,b) => b.solved-a.solved || a.penalty-b.penalty);
  return `<section class="panel"><div class="section-heading" style="margin-top:0"><h2>${event.stage === 'Published' ? 'Official standings' : 'Provisional standings'}</h2><span class="status ${event.stage.toLowerCase()}">${event.stage === 'Published' ? 'Published' : 'Internal preview'}</span></div><div class="table-wrap"><table><thead><tr><th>Rank</th><th>Team</th><th>Solved</th><th>Penalty</th></tr></thead><tbody>${rows.map((r,i) => `<tr class="${i === 0 ? 'winning' : ''}"><td class="rank">${String(i+1).padStart(2,'0')}</td><td>${r.name}</td><td>${r.solved} / 10</td><td>${r.penalty} min</td></tr>`).join('')}</tbody></table></div></section>`;
}
function bracketPanel(event) {
  const [a,b,champion] = state.winners;
  const match = (names, index) => `<div class="match">${names.map(name => state.role === 'Judge' && event.stage === 'Active' ? `<button data-action="winner" data-match="${index}" data-name="${name || ''}" class="${state.winners[index] === name && name ? 'winner' : ''}" ${!name || state.winners[index] ? 'disabled' : ''}>${name || 'Awaiting winner'}<span>${state.winners[index] === name && name ? '✓' : '›'}</span></button>` : `<div class="entrant ${state.winners[index] === name && name ? 'winner' : ''}">${name || 'Awaiting winner'} ${state.winners[index] === name && name ? '✓' : ''}</div>`).join('')}</div>`;
  return `<section class="panel"><h2>The road to the final</h2><p>${state.role === 'Judge' && event.stage === 'Active' ? 'Click a team to confirm a match winner. Confirmed matches are final.' : 'Follow the confirmed winners through each round.'}</p><div class="bracket-board"><div><div class="round-label">01 / Semifinals</div>${match(['Team Alpha','Team Beta'],0)}${match(['Team Gamma','Team Delta'],1)}</div><div><div class="round-label">02 / Final</div>${match([a,b],2)}</div><div><div class="round-label">03 / Champion</div><div class="champion"><span aria-hidden="true">✧</span><strong>${champion || 'Who will take the title?'}</strong><small>${champion ? 'Knockout Cup winner' : 'The final decides it.'}</small></div></div></div></section>`;
}
function teams() { return heading('BETTER, TOGETHER', 'Your people. Your potential.', 'A shared ambition starts with a team.') + `<div class="detail-grid"><div><section class="panel"><div class="team-banner"><div class="team-monogram">A</div><div><div class="eyebrow muted">MY TEAM</div><h2 style="margin:8px 0">${escapeHtml(state.team)}</h2><span class="muted" style="font-size:12px">Computing & engineering · 2 members</span></div></div><div class="section-heading"><h3>Team roster</h3>${state.registered ? '<span class="status">Roster locked</span>' : button('Rename team','rename-team','',true)}</div><div class="member"><span class="avatar">KA</span><div>Khaled Alkhawaldeh<small>202410091</small></div><span class="status">Captain</span></div><div class="member"><span class="avatar">AA</span><div>Abdulrahman Alnaqbi<small>202340042</small></div><span class="status">Member</span></div></section><section class="panel"><h2>Registered competitions</h2>${state.registered ? `<div class="key-value"><strong>Campus Innovation Challenge</strong><a href="#event/innovation">Open →</a></div>` : '<p>Your team hasn’t registered for a competition yet.</p><a class="button secondary" href="#competitions">Find a competition →</a>'}</section></div><aside><section class="panel"><h2>A team that stays together</h2><p>The captain registers and submits on behalf of the team. Once registered, the roster is locked to keep entries fair.</p><div class="callout">This prototype shows a sample two-person team. Membership management is available in the Java app.</div></section></aside></div>`; }
function judging() {
  return heading('FAIR PLAY, CLEAR DECISIONS', 'Judging studio', 'Three formats. The right tools for each decision.') + (state.role !== 'Judge' ? `<div class="callout">You’re exploring as ${state.role}. Choose Judge in the top bar to enter scores and confirm match winners.</div>` : '') + `<div class="cards">${state.events.map(card).join('')}</div><section class="panel" style="margin-top:24px"><h2>Start a judging walkthrough</h2><p>Code Sprint and Robotics Cup are ready for evaluation. The Innovation Challenge starts in registration, so you can explore the complete student-to-organizer-to-judge flow.</p><div class="pill-row"><span class="pill">Project → weighted rubric</span><span class="pill">Programming → solved + penalties</span><span class="pill">Knockout → match winners</span></div></section>`;
}
function results() {
  const published = state.events.filter(e => e.stage === 'Published');
  return heading('THE MOMENT THAT MATTERS', 'Celebrate the results.', 'Certified outcomes, from the first idea to the final round.') + (published.length ? published.map(e => `<div class="section-heading"><h2>${e.title}</h2><a href="#event/${e.id}">Competition details →</a></div>${e.type === 'code' ? programmingTable(e) : e.type === 'bracket' ? bracketPanel(e) : projectResults()}`).join('') : '<div class="empty"><div style="font:46px Georgia;color:#ac9470;margin-bottom:16px">✧</div><h2>The podium is waiting.</h2><p>Results appear here when an organizer publishes a completed competition.</p><a class="button secondary" href="#judging">Explore judging →</a></div>');
}
function projectResults() {
  const rows = [{name:'Team Beta',score:84.75},{name:state.team,score:projectTotal(state.projectScores)}].sort((a,b) => b.score-a.score);
  return `<section class="panel"><div class="table-wrap"><table><thead><tr><th>Rank</th><th>Team</th><th>Weighted score</th></tr></thead><tbody>${rows.map((r,i) => `<tr class="${i === 0 ? 'winning' : ''}"><td class="rank">${i+1}</td><td>${escapeHtml(r.name)}</td><td>${r.score.toFixed(2)} / 100</td></tr>`).join('')}</tbody></table></div></section>`;
}
function render() {
  const [page,id] = (location.hash.slice(1) || 'overview').split('/');
  const pages = {overview,competitions,teams,judging,results};
  $('#main').innerHTML = page === 'event' ? eventDetail(id) : (pages[page] || overview)();
  document.querySelectorAll('nav a').forEach(a => { const active = a.dataset.page === (page === 'event' ? 'competitions' : page); a.classList.toggle('active', active); if (active) a.setAttribute('aria-current','page'); else a.removeAttribute('aria-current'); });
  $('#role').value = state.role; $('#avatar').textContent = {Student:'KA',Organizer:'OR',Judge:'JU'}[state.role];
  if ($('#format')) $('#format').value = format;
}
function modal(title, body, onSubmit) {
  $('#dialog-content').innerHTML = `<div class="dialog-head"><h2>${title}</h2><button class="close" aria-label="Close dialog" type="button" data-action="close">×</button></div><form id="modal-form">${body}<div id="form-error" class="error" role="alert"></div><div class="dialog-actions"><button type="button" class="button secondary" data-action="close">Cancel</button><button class="button" type="submit">Confirm</button></div></form>`;
  $('#modal-form').addEventListener('submit', e => { e.preventDefault(); onSubmit(new FormData(e.currentTarget)); });
  $('#dialog').showModal();
}
function finish(message) { $('#dialog').close(); render(); toast(message); }
const projectTotal = scores => scores ? scores.innovation*.4 + scores.execution*.35 + scores.presentation*.25 : 0;
function advance(id) {
  const event = eventById(id);
  if (state.role !== 'Organizer') return;
  if (event.stage === 'Registration' && !state.registered) return toast('Register your team as Student before starting the walkthrough.');
  if (event.stage === 'Active' && event.type === 'project' && !state.submission) return toast('Team Alpha must submit a project before judging opens.');
  if (event.stage === 'Judging' && event.type === 'project' && !state.projectScores) return toast('Complete the project review as Judge before publishing.');
  if (event.stage === 'Active' && event.type === 'bracket' && !state.winners[2]) return toast('Confirm all matches and the champion before publishing.');
  const next = {Registration:'Active',Active:'Judging',Judging:'Published'}[event.stage]; if (!next) return;
  modal(next === 'Published' ? 'Publish competition results?' : `Move to ${next}?`, `<p>${event.title}</p><p>${next === 'Published' ? 'The results will become visible to participants. This completes the competition walkthrough.' : next === 'Active' ? 'Registration will close. Registered teams can now submit their projects.' : (event.type === 'bracket' ? 'The completed bracket will be certified. Publish next to release the results.' : 'Submissions will close and the assigned judge can begin reviewing.')}</p>`, () => { event.stage = next; finish(next === 'Published' ? 'Results published. Open Results to see the podium.' : `Competition moved to ${next}.`); });
}
document.addEventListener('click', e => {
  const target = e.target.closest('[data-action]'); if (!target) return;
  const action = target.dataset.action;
  if (action === 'close') return $('#dialog').close();
  if (action === 'advance') return advance(target.dataset.id);
  if (action === 'register') {
    if (state.role !== 'Student' || state.registered || eventById('innovation').stage !== 'Registration') return;
    return modal('Ready to enter?', `<p>Register <strong>${escapeHtml(state.team)}</strong> for the Campus Innovation Challenge.</p><div class="callout">Khaled Alkhawaldeh · Captain<br>Abdulrahman Alnaqbi · Member</div><p>The team roster will be locked after registration.</p>`, () => { state.registered = true; finish('Your team is registered. Switch to Organizer to start the event.'); });
  }
  if (action === 'rename-team') {
    if (state.registered) return;
    return modal('Rename your team', `<label class="field">Team name<input name="name" value="${escapeHtml(state.team)}" required minlength="3" maxlength="40"></label>`, data => { const name = data.get('name').trim(); if (name.length < 3) return $('#form-error').textContent = 'Use at least three non-space characters.'; state.team = name; finish('Team name updated.'); });
  }
  if (action === 'submit-project') {
    if (state.role !== 'Student' || !state.registered || eventById('innovation').stage !== 'Active') return;
    const submission = state.submission || {title:'',url:'',description:''};
    return modal('Submit your project', `<p>Give the judge enough context to review your work.</p><label class="field">Project title<input name="title" required maxlength="80" value="${escapeHtml(submission.title)}" placeholder="e.g. Campus Companion"></label><label class="field">Project link<input name="url" required type="url" value="${escapeHtml(submission.url)}" placeholder="https://example.com/project"><small>Use an https:// or http:// link.</small></label><label class="field">Description<textarea name="description" required maxlength="600" placeholder="What problem does your project solve?">${escapeHtml(submission.description)}</textarea></label>`, data => { const title = data.get('title').trim(), description = data.get('description').trim(), url = data.get('url').trim(); if (!title || !description || !/^https?:\/\//i.test(url)) return $('#form-error').textContent = 'Add a title, description, and a valid HTTP or HTTPS link.'; state.submission = {title,description,url}; finish('Project submitted. Switch to Organizer to open judging.'); });
  }
  if (action === 'review-project') {
    if (state.role !== 'Judge' || eventById('innovation').stage !== 'Judging') return;
    if (!state.submission) return toast('A project submission is required first.');
    const s = state.projectScores || {innovation:85,execution:90,presentation:80};
    modal('Review the project', `<p><strong>${escapeHtml(state.submission.title)}</strong> · ${escapeHtml(state.team)}</p><p>${escapeHtml(state.submission.description)}</p><p><a href="${escapeHtml(state.submission.url)}" target="_blank" rel="noopener noreferrer">Open submitted project ↗</a></p>${[['innovation',40],['execution',35],['presentation',25]].map(([name,weight]) => `<label class="field">${name[0].toUpperCase()+name.slice(1)} · ${weight}%<input name="${name}" type="number" min="0" max="100" step="0.1" value="${s[name]}" required data-score></label>`).join('')}<div class="score-total"><span>Weighted total</span><strong id="weighted-score">${projectTotal(s).toFixed(2)}</strong></div><p>One assigned judge is modeled here. Team Beta’s review is already complete.</p>`, data => { state.projectScores = Object.fromEntries(['innovation','execution','presentation'].map(k => [k,Number(data.get(k))])); finish('Review saved. Switch to Organizer to publish the results.'); });
    return;
  }
  if (action === 'review-code') {
    if (state.role !== 'Judge' || eventById('code').stage !== 'Judging') return;
    const p = state.programming;
    return modal('Record an official score', `<p>Code Sprint 2026 · Team Alpha</p><label class="field">Problems solved (out of 10)<input name="solved" type="number" min="0" max="10" step="1" required value="${p.solved}"></label><label class="field">Elapsed minutes<input name="minutes" type="number" min="0" max="300" step="1" required value="${p.minutes}"></label><label class="field">Wrong attempts<input name="wrong" type="number" min="0" max="100" step="1" required value="${p.wrong}"></label><div class="callout">Penalty = elapsed minutes + 20 × wrong attempts. Teams with more solved problems always rank first.</div>`, data => { state.programming = Object.fromEntries(['solved','minutes','wrong'].map(k => [k,Number(data.get(k))])); finish('Official score saved. Standings updated.'); });
  }
  if (action === 'winner') {
    const index = Number(target.dataset.match), name = target.dataset.name;
    if (state.role !== 'Judge' || eventById('robotics').stage !== 'Active' || state.winners[index] || !name || (index === 2 && (!state.winners[0] || !state.winners[1]))) return toast('Complete both semifinals before the final.');
    return modal('Confirm match winner', `<p><strong>${name}</strong> wins ${index === 2 ? 'the final' : 'this semifinal'}.</p><p>Confirmed match results are final in this walkthrough.</p>`, () => { state.winners[index] = name; finish(index === 2 ? `${name} is the champion. Switch to Organizer to certify, then publish.` : `${name} advances to the final.`); });
  }
});
document.addEventListener('input', e => {
  if (e.target.id === 'search') { search = e.target.value; $('#competition-list').innerHTML = filteredCards(); }
  if (e.target.matches('[data-score]')) { const data = new FormData($('#modal-form')); const scores = Object.fromEntries(['innovation','execution','presentation'].map(k => [k,Number(data.get(k))])); $('#weighted-score').textContent = projectTotal(scores).toFixed(2); }
});
document.addEventListener('change', e => { if (e.target.id === 'format') { format = e.target.value; $('#competition-list').innerHTML = filteredCards(); } });
$('#role').addEventListener('change', e => { state.role = e.target.value; render(); toast(`Exploring as ${state.role}.`); });
$('#reset').addEventListener('click', () => modal('Start a fresh walkthrough?', '<p>This clears only the prototype’s temporary changes. The Java app and its data are separate.</p>', () => { state = initialState(); search = ''; format = 'all'; location.hash = 'overview'; finish('Demo reset. Ready to explore.'); }));
window.addEventListener('hashchange', () => { render(); window.scrollTo({top:0,behavior:'instant'}); $('#main').focus({preventScroll:true}); });
render();
