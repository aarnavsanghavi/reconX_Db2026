// File: static-dashboard/js/trades.js — TICKET-ADV106 sort + resize
console.log("trade.js running");
(function () {
    const table = document.getElementById('trades-table');
    const tbody = document.getElementById('trades-tbody');
    let rows = []; // canonical data — sort operates on this
  
    console.log("table= ",table);
    // ---------- sortable columns ----------
    table.querySelectorAll('thead th').forEach(th => {
      th.addEventListener('click', (e) => {
        if (e.target.classList.contains('resize-handle')) return; // ignore resize clicks
        const col = th.dataset.col;
        const type = th.dataset.type || 'string';
        const dir = th.getAttribute('aria-sort') === 'ascending' ? 'descending' : 'ascending';
  
        // clear all, set this one
        table.querySelectorAll('thead th').forEach(o => o.removeAttribute('aria-sort'));
        th.setAttribute('aria-sort', dir);
  
        const mult = dir === 'ascending' ? 1 : -1;
        rows.sort((a, b) => {
          const av = a[col], bv = b[col];
          if (type === 'number') return (Number(av) - Number(bv)) * mult;
          return String(av).localeCompare(String(bv)) * mult;
        });
        renderRows();
      });
    });
  
    // ---------- resizable columns ----------
    table.querySelectorAll('.resize-handle').forEach(handle => {
      handle.addEventListener('mousedown', (e) => {
        e.preventDefault();
        const th = handle.closest('th');
        const startX = e.clientX;
        const startWidth = th.offsetWidth;
  
        // Listen on DOCUMENT so the drag survives leaving the handle.
        function onMove(ev) { th.style.width = (startWidth + ev.clientX - startX) + 'px'; }
        function onUp()     { document.removeEventListener('mousemove', onMove);
                              document.removeEventListener('mouseup', onUp); }
        document.addEventListener('mousemove', onMove);
        document.addEventListener('mouseup', onUp);
      });
    });
  
    function renderRows() {
    console.log("rendering renderrows");
    console.log("type",typeof(rows));
    console.log("is array",Array.isArray(rows));
      tbody.innerHTML = rows.map(r => `
        <tr>
          <td>${r.tradeRef}</td><td>${r.symbol}</td>
          <td>${r.quantity}</td><td>${r.price}</td>
          <td>${r.status}</td>
        </tr>`).join('');
    }
    console.log("before fetch");
  
    // initial load — hits the REST API from Day 5
    console.log("fetch url",'http://localhost8081');
    fetch('http://localhost:8081/api/v1/trades?size=20')
      .then(r => {console.log("fetch response received");console.log("Status",r.status);return r.json()})
      .then(data => { console.log(data);
        if(!data) {
            console.log("Nodata");
            return;
        }
        console.log("TSON string",JSON.stringify(data,null,2));
        if(Array.isArray(data)){
            rows=data;
        }
        else{
            //rows = data.content || data;
            rows=data.items;
        }
        renderRows(); });
  })();