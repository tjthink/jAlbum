package com.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.backend.FileInfo;
import com.backend.UniqPhotosStore;
import com.utils.conf.AppConfig;
import com.utils.web.GenerateHTML;
import com.utils.web.HeadUtils;

@Produces(value = { "text/xml", "application/json", "application/xml", "text/html" })
public class RestRootWebService extends HttpServlet
{
    private static final Logger logger = LoggerFactory.getLogger(RestRootWebService.class);

    private static final long serialVersionUID = -7748065720779404006L;

    @GET
    @Path("/favicon.ico")
    public Response getFavicon(@Context HttpServletRequest req,
            @Context HttpServletResponse response)
    {
        logger.debug("getFavicon in!");
        ResponseBuilder builder = null;
        try
        {
            builder = Response.ok();
            FileInputStream fp = new FileInputStream(new File("favicon.ico"));
            builder.entity(fp);
            builder.header("Content-type", "image/x-icon");
            HeadUtils.setExpiredTime(builder);
            logger.debug("getFavicon out!");
        }
        catch (Exception e)
        {
            logger.error("catch some exception.", e);
        }

        return builder.build();
    }

    @GET
    @Path("/js/{file}")
    public Response getJSFile(@PathParam("file") String file, @Context HttpServletRequest req,
            @Context HttpServletResponse response)
    {
        logger.debug("get js file in!");
        ResponseBuilder builder = null;
        try
        {
            File filepath = new File("js" + File.separator + file);
            if (filepath.isFile())
            {
                builder = Response.ok();
                FileInputStream fp = new FileInputStream(filepath);
                builder.entity(fp);
                builder.header("Content-type", "application/javascript");
                HeadUtils.setExpiredTime(builder);
                logger.debug("getFavicon out!");
            }
            else
            {
                builder = Response.ok();
                builder.status(404);
            }
        }
        catch (Exception e)
        {
            logger.error("catch some exception.", e);
        }

        return builder.build();
    }

    @GET
    @Path("/")
    public Response getMsg(@Context HttpServletRequest req, @Context HttpServletResponse response)
            throws IOException
    {
        ResponseBuilder builder = Response.status(200);
        String body = GenerateHTML.genIndexPage(getFileList(req),
                HeadUtils.checkMobile(req) ? 3 : 5);
        if (StringUtils.isNotBlank(body))
        {
            builder.entity(body);
            builder.header("Content-type", "text/html");
        }
        else
        {
            builder.entity("404 not found!");
            builder.status(404);
        }

        return builder.build();
    }

    public List<FileInfo> getFileList(HttpServletRequest req)
    {
        List<FileInfo> lst = null;

        int count = 0;
        int maxCount = AppConfig.getInstance().getMaxCountOfPicInOnePage(25);
        String countStr = req.getParameter("count");
        if (StringUtils.isNotBlank(countStr))
        {
            count = Integer.parseInt(countStr);
        }

        if (count == 0 || count > maxCount)
        {
            count = maxCount;
        }

        if (count > 9)
        {
            if (HeadUtils.checkMobile(req))
            {
                count = 9;
            }
        }

        String next = req.getParameter("next");

        if (StringUtils.isNotBlank(next))
        {
            lst = UniqPhotosStore.getInstance().getNextNineFileByHashStr(next, count);
            if (lst == null || lst.isEmpty())
            {
                lst = UniqPhotosStore.getInstance().getNextNineFileByHashStr(null, count);
            }

            return lst;
        }

        String prev = req.getParameter("prev");
        if (StringUtils.isNotBlank(prev))
        {
            lst = UniqPhotosStore.getInstance().getPrevNineFileByHashStr(prev, count);
            if (lst == null || lst.isEmpty())
            {
                lst = UniqPhotosStore.getInstance().getPrevNineFileByHashStr(null, count);
            }

            return lst;
        }

        return UniqPhotosStore.getInstance().getNextNineFileByHashStr(null, count);
    }

    @Path("/photos/{id}")
    public Object getPhoto(@PathParam("id") String id, @Context HttpServletRequest req,
            @Context HttpServletResponse response, @Context HttpHeaders headers, InputStream body)
    {
        if (StringUtils.isNotBlank(id))
        {
            return new ObjectService(id);
        }
        else
        {
            return new ErrorResource();
        }
    }

    @Path("/year/{year}")
    public Object getYearView(@PathParam("year") String year, @Context HttpServletRequest req,
            @Context HttpServletResponse response, @Context HttpHeaders headers, InputStream body)
    {
        if (StringUtils.isNotBlank(year))
        {
            return new YearService(year);
        }
        else
        {
            return new ErrorResource();
        }
    }

    @Path("/month/{month}")
    public Object getMonthView(@PathParam("month") String month, @Context HttpServletRequest req,
            @Context HttpServletResponse response, @Context HttpHeaders headers, InputStream body)
    {
        if (StringUtils.isNotBlank(month) && month.length() == 6)
        {
            return new MonthService(month);
        }
        else
        {
            return new ErrorResource();
        }
    }

    @Path("/day/{day}")
    public Object getDayView(@PathParam("day") String day, @Context HttpServletRequest req,
            @Context HttpServletResponse response, @Context HttpHeaders headers, InputStream body)
    {
        if (StringUtils.isNotBlank(day) && day.length() == 8)
        {
            return new DayService(day);
        }
        else
        {
            return new ErrorResource();
        }
    }
}
